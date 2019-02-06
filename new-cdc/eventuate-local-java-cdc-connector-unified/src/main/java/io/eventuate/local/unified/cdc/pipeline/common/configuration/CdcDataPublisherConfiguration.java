package io.eventuate.local.unified.cdc.pipeline.common.configuration;

import io.eventuate.local.common.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CdcDataPublisherConfiguration {

  @Bean
  public CdcDataPublisherFactory<PublishedEvent> cdcDataPublisherFactory(MeterRegistry meterRegistry) {

    return (dataProducer) -> new CdcDataPublisher<>(dataProducer,
            new PublishedEventPublishingStrategy(),
            meterRegistry);
  }
}
