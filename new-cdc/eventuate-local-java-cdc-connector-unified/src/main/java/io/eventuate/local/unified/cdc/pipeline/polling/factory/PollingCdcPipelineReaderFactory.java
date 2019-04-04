package io.eventuate.local.unified.cdc.pipeline.polling.factory;

import io.eventuate.local.java.common.util.LeaderSelectorFactory;
import io.eventuate.local.polling.PollingDao;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;

public class PollingCdcPipelineReaderFactory extends CommonCdcPipelineReaderFactory<PollingPipelineReaderProperties, PollingDao> {

  public static final String TYPE = "polling";

  public PollingCdcPipelineReaderFactory(MeterRegistry meterRegistry,
                                         LeaderSelectorFactory leaderSelectorFactory,
                                         BinlogEntryReaderProvider binlogEntryReaderProvider) {

    super(meterRegistry, leaderSelectorFactory, binlogEntryReaderProvider);
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
            readerProperties.getReaderName());
  }

  @Override
  public Class<PollingPipelineReaderProperties> propertyClass() {
    return PollingPipelineReaderProperties.class;
  }
}
