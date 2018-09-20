package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogBasedCdcProcessor;
import io.eventuate.local.db.log.common.OffsetStore;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.function.Consumer;

public class MySQLCdcProcessor<EVENT extends BinLogEvent> extends DbLogBasedCdcProcessor<EVENT> {

  private MySqlBinaryLogClient mySqlBinaryLogClient;
  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore;
  private EventuateSchema eventuateSchema;
  private String sourceTableName;

  public MySQLCdcProcessor(MySqlBinaryLogClient mySqlBinaryLogClient,
                           OffsetStore offsetStore,
                           DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore,
                           BinlogEntryToEventConverter binlogEntryToEventConverter,
                           String sourceTableName,
                           EventuateSchema eventuateSchema) {

    super(mySqlBinaryLogClient, offsetStore, binlogEntryToEventConverter);

    this.mySqlBinaryLogClient = mySqlBinaryLogClient;
    this.debeziumBinlogOffsetKafkaStore = debeziumBinlogOffsetKafkaStore;
    this.eventuateSchema = eventuateSchema;
    this.sourceTableName = sourceTableName;
  }

  @Override
  public void start(Consumer<EVENT> eventConsumer) {
    Optional<BinlogFileOffset> binlogFileOffset = offsetStore.getLastBinlogFileOffset();

    if (!binlogFileOffset.isPresent()) {
      binlogFileOffset = debeziumBinlogOffsetKafkaStore.getLastBinlogFileOffset();
    }

    Optional<BinlogFileOffset> startingBinlogFileOffset = binlogFileOffset;

    process(eventConsumer, startingBinlogFileOffset);
  }

  protected void process(Consumer<EVENT> eventConsumer, Optional<BinlogFileOffset> startingBinlogFileOffset) {
    try {
      MySqlBinlogEntryHandler binlogEntryHandler = new MySqlBinlogEntryHandler(
              eventuateSchema,
              new MySqlBinlogEntryExtractor(mySqlBinaryLogClient.getDataSource(), sourceTableName, eventuateSchema),
              sourceTableName,
              createBinlogConsumer(eventConsumer, startingBinlogFileOffset));

      mySqlBinaryLogClient.addBinlogEntryHandler(binlogEntryHandler);

      mySqlBinaryLogClient.setBinlogFileOffset(startingBinlogFileOffset);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
