package io.eventuate.local.postgres.binlog;


import io.eventuate.local.common.BinlogFileOffset;
import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PostgresBinaryLogClient<EVENT> {

  private DataSource dataSource;
  private PostgresReplicationMessageParser<EVENT> postgresReplicationMessageParser;

  public PostgresBinaryLogClient(DataSource dataSource, PostgresReplicationMessageParser<EVENT> postgresReplicationMessageParser) {
    this.dataSource = dataSource;
    this.postgresReplicationMessageParser = postgresReplicationMessageParser;
  }

  public void start(Optional<BinlogFileOffset> binlogFileOffset, Consumer<EVENT> eventConsumer) {

      new Thread(() -> {
          try {
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
          } catch (Exception e) {
              throw new RuntimeException(e);
          }
      }).start();
  }

  public void stop() {

  }
}
