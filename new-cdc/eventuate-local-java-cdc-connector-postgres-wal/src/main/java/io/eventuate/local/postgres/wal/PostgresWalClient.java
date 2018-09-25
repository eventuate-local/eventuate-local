package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;
import org.apache.curator.framework.CuratorFramework;
import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PostgresWalClient extends DbLogClient {
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private PostgresWalBinlogEntryExtractor postgresWalBinlogEntryExtractor;
  private int walIntervalInMilliseconds;
  private int connectionTimeoutInMilliseconds;
  private int maxAttemptsForBinlogConnection;
  private Connection connection;
  private PGReplicationStream stream;
  private CountDownLatch countDownLatchForStop;
  private int replicationStatusIntervalInMilliseconds;
  private String replicationSlotName;
  private OffsetStore offsetStore;

  public PostgresWalClient(String url,
                           String user,
                           String password,
                           int walIntervalInMilliseconds,
                           int connectionTimeoutInMilliseconds,
                           int maxAttemptsForBinlogConnection,
                           int replicationStatusIntervalInMilliseconds,
                           String replicationSlotName,
                           CuratorFramework curatorFramework,
                           String leadershipLockPath,
                           OffsetStore offsetStore) {

    super(user, password, url, curatorFramework, leadershipLockPath);

    this.walIntervalInMilliseconds = walIntervalInMilliseconds;
    this.connectionTimeoutInMilliseconds = connectionTimeoutInMilliseconds;
    this.maxAttemptsForBinlogConnection = maxAttemptsForBinlogConnection;
    this.replicationStatusIntervalInMilliseconds = replicationStatusIntervalInMilliseconds;
    this.replicationSlotName = replicationSlotName;
    this.postgresWalBinlogEntryExtractor = new PostgresWalBinlogEntryExtractor();
    this.offsetStore = offsetStore;
  }

  public OffsetStore getOffsetStore() {
    return offsetStore;
  }

  @Override
  protected void leaderStart() {
    running.set(true);

    new Thread(() -> connectWithRetriesOnFail(offsetStore.getLastBinlogFileOffset())).start();
  }

  private void connectWithRetriesOnFail(Optional<BinlogFileOffset> binlogFileOffset) {
    for (int i = 1;; i++) {
      try {
        logger.info("trying to connect to postgres wal");
        connectAndRun(binlogFileOffset);
        break;
      } catch (SQLException | InterruptedException e) {
        logger.error("connection to posgres wal failed");
        if (i == maxAttemptsForBinlogConnection) {
          logger.error("connection attempts exceeded");
          throw new RuntimeException(e);
        }
        try {
          Thread.sleep(connectionTimeoutInMilliseconds);
        } catch (InterruptedException ex) {
          throw new RuntimeException(ex);
        }
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
        throw new RuntimeException(e);
      }
    }
  }

  private void connectAndRun(Optional<BinlogFileOffset> binlogFileOffset)
          throws SQLException, InterruptedException, IOException {

    countDownLatchForStop = new CountDownLatch(1);

    Properties props = new Properties();
    PGProperty.USER.set(props, dbUserName);
    PGProperty.PASSWORD.set(props, dbPassword);
    PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "9.4");
    PGProperty.REPLICATION.set(props, "database");
    PGProperty.PREFER_QUERY_MODE.set(props, "simple");

    connection = DriverManager.getConnection(dataSourceUrl, props);

    PGConnection replConnection = connection.unwrap(PGConnection.class);

    LogSequenceNumber lsn = binlogFileOffset
            .flatMap(offset -> Optional.ofNullable(offset.getOffset()).map(LogSequenceNumber::valueOf))
            .orElse(LogSequenceNumber.valueOf("0/0"));

    stream = replConnection.getReplicationAPI()
            .replicationStream()
            .logical()
            .withSlotName(replicationSlotName)
            .withSlotOption("include-xids", false)
            .withStatusInterval(replicationStatusIntervalInMilliseconds, TimeUnit.MILLISECONDS)
            .withStartPosition(lsn)
            .start();

    logger.info("connection to postgres wal succeed");

    while (running.get()) {
      ByteBuffer messageBuffer = stream.readPending();

      if (messageBuffer == null) {
        logger.info("Got empty message, sleeping");
        TimeUnit.MILLISECONDS.sleep(walIntervalInMilliseconds);
        continue;
      }

      String messageString = extractStringFromBuffer(messageBuffer);

      logger.info("Got message: " + messageString);

      PostgresWalMessage postgresWalMessage = JSonMapper.fromJson(messageString, PostgresWalMessage.class);


      List<PostgresWalChange> inserts = Arrays
              .stream(postgresWalMessage.getChange())
              .filter(change -> change.getKind().equals("insert"))
              .collect(Collectors.toList());

      binlogEntryHandlers.forEach(binlogEntryHandler -> {
        List<PostgresWalChange> filteredChanges = inserts
                .stream()
                .filter(change -> binlogEntryHandler.isFor(change.getSchema(), change.getTable(), defaultDatabase))
                .collect(Collectors.toList());

        postgresWalBinlogEntryExtractor
                .extract(filteredChanges, stream.getLastReceiveLSN().asLong(), replicationSlotName)
                .forEach(new Consumer<BinlogEntry>() {
                  private boolean couldReadDuplicateEntries = true;

                  @Override
                  public void accept(BinlogEntry binlogEntry) {
                    if (couldReadDuplicateEntries) {
                      if (binlogFileOffset.map(s -> s.isSameOrAfter(binlogEntry.getBinlogFileOffset())).orElse(false)) {
                        return;
                      } else {
                        couldReadDuplicateEntries = false;
                      }
                    }

                    binlogEntryHandler.publish(binlogEntry);
                  }
                });
      });

      stream.setAppliedLSN(stream.getLastReceiveLSN());
      stream.setFlushedLSN(stream.getLastReceiveLSN());
    }
    try {
      stream.close();
      connection.close();
    } catch (SQLException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
    countDownLatchForStop.countDown();
  }

  @Override
  protected void leaderStop() {
    if (!running.compareAndSet(true, false)) {
      return;
    }

    try {
      countDownLatchForStop.await();
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private String extractStringFromBuffer(ByteBuffer byteBuffer) {
    int offset = byteBuffer.arrayOffset();
    byte[] source = byteBuffer.array();
    int length = source.length - offset;

    return new String(source, offset, length);
  }
}
