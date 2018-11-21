package io.eventuate.local.unified.cdc.pipeline.common.configuration;

import io.eventuate.local.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultCdcPipelineFactoryConfiguration {
  @Bean("defaultCdcPipelineFactory")
  public CdcPipelineFactory defaultCdcPipelineFactory(DataProducerFactory dataProducerFactory,
                                                      PublishingFilter publishingFilter,
                                                      BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                      MeterRegistry meterRegistry,
                                                      HealthCheck healthCheck) {

    return new CdcPipelineFactory<>("eventuate-local",
            binlogEntryReaderProvider,
            new CdcDataPublisher<>(dataProducerFactory,
                    publishingFilter,
                    new PublishedEventPublishingStrategy(),
                    meterRegistry,
                    healthCheck),
            new BinlogEntryToPublishedEventConverter());
  }
}
