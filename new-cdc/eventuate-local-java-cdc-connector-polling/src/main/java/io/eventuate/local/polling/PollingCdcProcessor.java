package io.eventuate.local.polling;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntryToEventConverter;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.JdbcUrlParser;
import java.util.function.Consumer;

public class PollingCdcProcessor<EVENT> implements CdcProcessor<EVENT> {
  private PollingDao pollingDao;
  private PollingDataProvider pollingDataProvider;
  private BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;
  private String dataSourceUrl;
  private EventuateSchema eventuateSchema;
  private String sourceTableName;

  public PollingCdcProcessor(PollingDao pollingDao,
                             PollingDataProvider pollingDataProvider,
                             BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                             String dataSourceUrl,
                             EventuateSchema eventuateSchema,
                             String sourceTableName) {
    this.pollingDao = pollingDao;
    this.pollingDataProvider = pollingDataProvider;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
    this.dataSourceUrl = dataSourceUrl;
    this.eventuateSchema = eventuateSchema;
    this.sourceTableName = sourceTableName;
  }

  @Override
  public void start(Consumer<EVENT> eventConsumer) {
    PollingEntryHandler pollingEntryHandler = new PollingEntryHandler(JdbcUrlParser.parse(dataSourceUrl).getDatabase(),
            eventuateSchema,
            sourceTableName,
            binlogEntry -> eventConsumer.accept(binlogEntryToEventConverter.convert(binlogEntry)),
            pollingDataProvider.publishedField(),
            pollingDataProvider.idField());

    pollingDao.addPollingEntryHandler(pollingEntryHandler);
  }

  @Override
  public void stop() {
  }
}
