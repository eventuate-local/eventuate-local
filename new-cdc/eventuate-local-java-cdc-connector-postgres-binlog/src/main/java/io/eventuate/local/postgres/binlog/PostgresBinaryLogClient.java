package io.eventuate.local.postgres.binlog;


import io.eventuate.local.common.BinlogFileOffset;
import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PostgresBinaryLogClient<EVENT> {
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private PostgresReplicationMessageParser<EVENT> postgresReplicationMessageParser;
  private int connectionTimeoutInMilliseconds;
  private int maxAttemptsForBinlogConnection;

  public PostgresBinaryLogClient(PostgresReplicationMessageParser<EVENT> postgresReplicationMessageParser,
          int connectionTimeoutInMilliseconds,
          int maxAttemptsForBinlogConnection) {
    this.connectionTimeoutInMilliseconds = connectionTimeoutInMilliseconds;
    this.maxAttemptsForBinlogConnection = maxAttemptsForBinlogConnection;
    this.postgresReplicationMessageParser = postgresReplicationMessageParser;
  }

  public void start(Optional<BinlogFileOffset> binlogFileOffset, Consumer<EVENT> eventConsumer) {
    new Thread(() -> connectWithRetriesOnFail(binlogFileOffset, eventConsumer)).start();
  }

  private void connectWithRetriesOnFail(Optional<BinlogFileOffset> binlogFileOffset, Consumer<EVENT> eventConsumer) {
    for (int i = 1;; i++) {
      try {
        logger.info("trying to connect to postgres wal");
        connectAndRun(binlogFileOffset, eventConsumer);
        logger.info("connection to postgres wal succeed");
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
      }
    }
  }

  private void connectAndRun(Optional<BinlogFileOffset> binlogFileOffset, Consumer<EVENT> eventConsumer) throws SQLException, InterruptedException {
      String url = "jdbc:postgresql://172.17.0.1:5432/eventuate";
      Properties props = new Properties();
      PGProperty.USER.set(props, "eventuate");
      PGProperty.PASSWORD.set(props, "eventuate");
      PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "9.4");
      PGProperty.REPLICATION.set(props, "database");
      PGProperty.PREFER_QUERY_MODE.set(props, "simple");

      Connection con = DriverManager.getConnection(url, props);

      PGConnection replConnection = con.unwrap(PGConnection.class);

      LogSequenceNumber lsn = binlogFileOffset
              .flatMap(offset -> Optional.ofNullable(offset.getOffset()).map(LogSequenceNumber::valueOf))
              .orElse(LogSequenceNumber.valueOf("0/0"));

      PGReplicationStream stream =
              replConnection.getReplicationAPI()
                      .replicationStream()
                      .logical()
                      .withSlotName("test_slot")
                      .withSlotOption("include-xids", false)
                      .withStartPosition(lsn)
                      .withStatusInterval(20, TimeUnit.SECONDS)
                      .start();

      while (true) {
          ByteBuffer msg = stream.readPending();

          if (msg == null) {
              TimeUnit.MILLISECONDS.sleep(10L);
              continue;
          }

          int offset = msg.arrayOffset();
          byte[] source = msg.array();
          int length = source.length - offset;

          postgresReplicationMessageParser
                  .parse(new String(source, offset, length), stream.getLastReceiveLSN().asLong())
                  .stream()
                  .forEach(eventConsumer::accept);

          stream.setAppliedLSN(stream.getLastReceiveLSN());
          stream.setFlushedLSN(stream.getLastReceiveLSN());
      }
  }

  public void stop() {

  }
}
