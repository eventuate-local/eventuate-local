package io.eventuate.local.unified.cdc.pipeline.polling.configuration;

import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.common.PublishedEventPublishingStrategy;
import io.eventuate.local.common.SourceTableNameSupplier;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.polling.PollingCdcDataPublisher;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PollingDefaultCdcPipelineFactoryConfiguration {
  @Profile("EventuatePolling")
  @Bean("defaultCdcPipelineFactory")
  public CdcPipelineFactory defaultPollingCdcPipelineFactory(DataProducerFactory dataProducerFactory,
                                                             BinlogEntryReaderProvider binlogEntryReaderProvider) {

    return new CdcPipelineFactory<>("eventuate-local",
            "polling",
            binlogEntryReaderProvider,
            new PollingCdcDataPublisher<>(dataProducerFactory, new PublishedEventPublishingStrategy()),
            sourceTableName -> new SourceTableNameSupplier(sourceTableName, "events", "event_id", "published"),
            new BinlogEntryToPublishedEventConverter());
  }
}
