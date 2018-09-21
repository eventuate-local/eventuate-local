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
  private EventuateSchema eventuateSchema;
  private String sourceTableName;

  public MySQLCdcProcessor(MySqlBinaryLogClient mySqlBinaryLogClient,
                           BinlogEntryToEventConverter binlogEntryToEventConverter,
                           String sourceTableName,
                           EventuateSchema eventuateSchema) {

    super(mySqlBinaryLogClient, binlogEntryToEventConverter);

    this.mySqlBinaryLogClient = mySqlBinaryLogClient;
    this.eventuateSchema = eventuateSchema;
    this.sourceTableName = sourceTableName;
  }

  @Override
  public void start(Consumer<EVENT> eventConsumer) {
    try {
      MySqlBinlogEntryHandler binlogEntryHandler = new MySqlBinlogEntryHandler(
              eventuateSchema,
              new MySqlBinlogEntryExtractor(mySqlBinaryLogClient.getDataSource(), sourceTableName, eventuateSchema),
              sourceTableName,
              createBinlogConsumer(eventConsumer));

      mySqlBinaryLogClient.addBinlogEntryHandler(binlogEntryHandler);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
