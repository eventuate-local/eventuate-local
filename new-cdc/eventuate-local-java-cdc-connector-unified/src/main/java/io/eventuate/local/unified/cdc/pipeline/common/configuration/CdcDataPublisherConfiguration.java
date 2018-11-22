package io.eventuate.local.unified.cdc.pipeline.common.configuration;

import io.eventuate.local.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CdcDataPublisherConfiguration {

  @Bean
  public CdcDataPublisher<PublishedEvent> cdcDataPublisher(DataProducerFactory dataProducerFactory,
                                                           PublishingFilter publishingFilter,
                                                           MeterRegistry meterRegistry) {

    return new CdcDataPublisher<>(dataProducerFactory,
                    publishingFilter,
                    new PublishedEventPublishingStrategy(),
                    meterRegistry);
  }
}
