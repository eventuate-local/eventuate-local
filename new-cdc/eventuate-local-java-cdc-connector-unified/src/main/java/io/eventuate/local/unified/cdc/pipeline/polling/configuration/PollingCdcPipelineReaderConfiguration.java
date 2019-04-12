package io.eventuate.local.unified.cdc.pipeline.polling.configuration;

import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.configuration.CommonCdcDefaultPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.PollingCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.properties.PollingPipelineReaderProperties;
import io.eventuate.sql.dialect.SqlDialectSelector;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PollingCdcPipelineReaderConfiguration extends CommonCdcDefaultPipelineReaderConfiguration {

  @Bean("eventuateLocalPollingCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory pollingCdcPipelineReaderFactory(MeterRegistry meterRegistry,
                                                                  LeaderSelectorFactory leaderSelectorFactory,
                                                                  BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                                  SqlDialectSelector sqlDialectSelector) {

    return new PollingCdcPipelineReaderFactory(meterRegistry,
            leaderSelectorFactory,
            binlogEntryReaderProvider,
            sqlDialectSelector);
  }

  @Profile("EventuatePolling")
  @Bean("defaultCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory defaultPollingCdcPipelineReaderFactory(MeterRegistry meterRegistry,
                                                                         LeaderSelectorFactory leaderSelectorFactory,
                                                                         BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                                         SqlDialectSelector sqlDialectSelector) {

    return new PollingCdcPipelineReaderFactory(meterRegistry,
            leaderSelectorFactory,
            binlogEntryReaderProvider,
            sqlDialectSelector);
  }

  @Profile("EventuatePolling")
  @Bean
  public CdcPipelineReaderProperties defaultPollingPipelineReaderProperties() {
    PollingPipelineReaderProperties pollingPipelineReaderProperties = createPollingPipelineReaderProperties();

    pollingPipelineReaderProperties.setType(PollingCdcPipelineReaderFactory.TYPE);

    initCdcPipelineReaderProperties(pollingPipelineReaderProperties);

    return pollingPipelineReaderProperties;
  }

  private PollingPipelineReaderProperties createPollingPipelineReaderProperties() {
    PollingPipelineReaderProperties pollingPipelineReaderProperties = new PollingPipelineReaderProperties();

    pollingPipelineReaderProperties.setPollingIntervalInMilliseconds(eventuateConfigurationProperties.getPollingIntervalInMilliseconds());
    pollingPipelineReaderProperties.setMaxEventsPerPolling(eventuateConfigurationProperties.getMaxEventsPerPolling());
    pollingPipelineReaderProperties.setMaxAttemptsForPolling(eventuateConfigurationProperties.getMaxAttemptsForPolling());
    pollingPipelineReaderProperties.setPollingRetryIntervalInMilliseconds(eventuateConfigurationProperties.getPollingRetryIntervalInMilliseconds());

    return pollingPipelineReaderProperties;
  }
}
