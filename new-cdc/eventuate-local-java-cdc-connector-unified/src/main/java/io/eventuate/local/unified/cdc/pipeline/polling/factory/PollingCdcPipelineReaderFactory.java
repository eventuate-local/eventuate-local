package io.eventuate.local.unified.cdc.pipeline.polling.factory;

import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.javaclient.spring.jdbc.EventuateSqlDialect;
import io.eventuate.local.polling.PollingDao;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;

public class PollingCdcPipelineReaderFactory extends CommonCdcPipelineReaderFactory<PollingPipelineReaderProperties, PollingDao> {

  public static final String TYPE = "polling";

  private EventuateSqlDialect eventuateSqlDialect;

  public PollingCdcPipelineReaderFactory(MeterRegistry meterRegistry,
                                         LeaderSelectorFactory leaderSelectorFactory,
                                         BinlogEntryReaderProvider binlogEntryReaderProvider,
                                         EventuateSqlDialect eventuateSqlDialect) {

    super(meterRegistry, leaderSelectorFactory, binlogEntryReaderProvider);

    this.eventuateSqlDialect = eventuateSqlDialect;
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }

  @Override
  public PollingDao create(PollingPipelineReaderProperties readerProperties) {

    return new PollingDao(meterRegistry,
            readerProperties.getDataSourceUrl(),
            createDataSource(readerProperties),
            readerProperties.getMaxEventsPerPolling(),
            readerProperties.getMaxAttemptsForPolling(),
            readerProperties.getPollingRetryIntervalInMilliseconds(),
            readerProperties.getPollingIntervalInMilliseconds(),
            readerProperties.getLeadershipLockPath(),
            leaderSelectorFactory,
            readerProperties.getReaderName(),
            eventuateSqlDialect);
  }

  @Override
  public Class<PollingPipelineReaderProperties> propertyClass() {
    return PollingPipelineReaderProperties.class;
  }
}
