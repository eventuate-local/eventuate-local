package io.eventuate.local.postgres.wal;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogBasedCdcProcessor;
import io.eventuate.local.db.log.common.OffsetStore;

import java.util.Optional;
import java.util.function.Consumer;

public class PostgresWalCdcProcessor<EVENT extends BinLogEvent> extends DbLogBasedCdcProcessor<EVENT> {

  private PostgresWalClient postgresWalClient;
  private String dataSourceUrl;
  private String sourceTableName;
  private EventuateSchema eventuateSchema;

  public PostgresWalCdcProcessor(PostgresWalClient postgresWalClient,
                                 OffsetStore offsetStore,
                                 BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                                 String dataSourceUrl,
                                 String sourceTableName,
                                 EventuateSchema eventuateSchema) {

    super(postgresWalClient, offsetStore, binlogEntryToEventConverter);

    this.postgresWalClient = postgresWalClient;

    this.dataSourceUrl = dataSourceUrl;
    this.sourceTableName = sourceTableName;
    this.eventuateSchema = eventuateSchema;
  }

  @Override
  public void start(Consumer<EVENT> eventConsumer) {
    Optional<BinlogFileOffset> startingBinlogFileOffset = offsetStore.getLastBinlogFileOffset();

    try {
      PostgresWalBinlogEntryHandler binlogEntryHandler = new PostgresWalBinlogEntryHandler(JdbcUrlParser.parse(dataSourceUrl).getDatabase(),
              eventuateSchema,
              sourceTableName,
              createBinlogConsumer(eventConsumer, startingBinlogFileOffset));

      postgresWalClient.addBinlogEntryHandler(binlogEntryHandler);

      postgresWalClient.setBinlogFileOffset(startingBinlogFileOffset);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
