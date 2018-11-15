package io.eventuate.local.unified.cdc.pipeline.polling.factory;

import io.eventuate.local.common.HealthCheck;
import io.eventuate.local.polling.PollingDao;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;

public class PollingCdcPipelineReaderFactory extends CommonCdcPipelineReaderFactory<PollingPipelineReaderProperties, PollingDao> {

  public static final String TYPE = "polling";

  public PollingCdcPipelineReaderFactory(MeterRegistry meterRegistry,
                                         HealthCheck healthCheck,
                                         CuratorFramework curatorFramework,
                                         BinlogEntryReaderProvider binlogEntryReaderProvider) {

    super(meterRegistry, healthCheck, curatorFramework, binlogEntryReaderProvider);
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }

  @Override
  public PollingDao create(PollingPipelineReaderProperties readerProperties) {

    return new PollingDao(meterRegistry,
            healthCheck,
            readerProperties.getDataSourceUrl(),
            createDataSource(readerProperties),
            readerProperties.getMaxEventsPerPolling(),
            readerProperties.getMaxAttemptsForPolling(),
            readerProperties.getPollingRetryIntervalInMilliseconds(),
            readerProperties.getPollingIntervalInMilliseconds(),
            curatorFramework,
            readerProperties.getLeadershipLockPath(),
            readerProperties.getBinlogClientId(),
            readerProperties.getMonitoringRetryIntervalInMilliseconds(),
            readerProperties.getMonitoringRetryAttempts(),
            readerProperties.getMaxEventIntervalToAssumeReaderHealthy());
  }

  @Override
  public Class<PollingPipelineReaderProperties> propertyClass() {
    return PollingPipelineReaderProperties.class;
  }
}
