package io.eventuate.local.unified.cdc.pipeline.polling.factory;

import io.eventuate.local.polling.PollingDao;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineReaderProperties;
import org.apache.curator.framework.CuratorFramework;

public class PollingCdcPipelineReaderFactory extends CommonCdcPipelineReaderFactory<PollingPipelineReaderProperties, PollingDao> {

  public static final String TYPE = "polling";

  public PollingCdcPipelineReaderFactory(CuratorFramework curatorFramework,
                                         BinlogEntryReaderProvider binlogEntryReaderProvider) {

    super(curatorFramework, binlogEntryReaderProvider);
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }

  @Override
  public PollingDao create(PollingPipelineReaderProperties pollingPipelineReaderProperties) {

    return new PollingDao(createDataSource(pollingPipelineReaderProperties),
            pollingPipelineReaderProperties.getMaxEventsPerPolling(),
            pollingPipelineReaderProperties.getMaxAttemptsForPolling(),
            pollingPipelineReaderProperties.getPollingRetryIntervalInMilliseconds(),
            pollingPipelineReaderProperties.getPollingIntervalInMilliseconds(),
            curatorFramework,
            pollingPipelineReaderProperties.getLeadershipLockPath());
  }

  @Override
  public Class<PollingPipelineReaderProperties> propertyClass() {
    return PollingPipelineReaderProperties.class;
  }
}
