package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogBasedCdcProcessor;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.function.Consumer;

public class MySQLCdcProcessor<EVENT extends BinLogEvent> extends DbLogBasedCdcProcessor<EVENT> {

  private MySqlBinaryLogClient mySqlBinaryLogClient;
  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore;
  private String defaultDataBase;
  private EventuateSchema eventuateSchema;
  private DataSource dataSource;
  private String sourceTableName;

  public MySQLCdcProcessor(MySqlBinaryLogClient mySqlBinaryLogClient,
                           OffsetStore offsetStore,
                           DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore,
                           BinlogEntryToEventConverter binlogEntryToEventConverter,
                           DataSource dataSource,
                           String datasourceUrl,
                           String sourceTableName,
                           EventuateSchema eventuateSchema) {

    super(mySqlBinaryLogClient, offsetStore, binlogEntryToEventConverter);

    this.mySqlBinaryLogClient = mySqlBinaryLogClient;
    this.debeziumBinlogOffsetKafkaStore = debeziumBinlogOffsetKafkaStore;

    this.dataSource = dataSource;
    this.defaultDataBase = JdbcUrlParser.parse(datasourceUrl).getDatabase();
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

  @Override
  protected void process(Consumer<EVENT> eventConsumer, Optional<BinlogFileOffset> startingBinlogFileOffset) {
    try {
      Consumer<BinlogEntry> consumer = new Consumer<BinlogEntry>() {
        private boolean couldReadDuplicateEntries = true;

        @Override
        public void accept(BinlogEntry binlogEntry) {
          if (couldReadDuplicateEntries) {
            if (startingBinlogFileOffset.map(s -> s.isSameOrAfter(binlogEntry.getBinlogFileOffset())).orElse(false)) {
              return;
            } else {
              couldReadDuplicateEntries = false;
            }
          }
          eventConsumer.accept(binlogEntryToEventConverter.convert(binlogEntry));
        }
      };

      BinlogEntryHandler binlogEntryHandler = new BinlogEntryHandler(defaultDataBase,
              eventuateSchema,
              new MySqlBinlogEntryExtractor(dataSource, sourceTableName, eventuateSchema),
              sourceTableName,
              consumer);

      mySqlBinaryLogClient.addBinlogEntryHandler(binlogEntryHandler);

      mySqlBinaryLogClient.start(startingBinlogFileOffset);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    dbLogClient.stop();
    offsetStore.stop();
  }
}
