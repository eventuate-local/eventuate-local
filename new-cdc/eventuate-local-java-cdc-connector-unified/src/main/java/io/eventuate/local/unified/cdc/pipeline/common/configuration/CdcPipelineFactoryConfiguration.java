package io.eventuate.local.unified.cdc.pipeline.common.configuration;

import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.PublishedEventPublishingStrategy;
import io.eventuate.local.common.DuplicatePublishingDetector;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CdcPipelineFactoryConfiguration {
  @Bean("eventuateLocalСdcPipelineFactory")
  public CdcPipelineFactory pollingCdcPipelineFactory(DataProducerFactory dataProducerFactory,
                                                      DuplicatePublishingDetector duplicatePublishingDetector,
                                                      BinlogEntryReaderProvider binlogEntryReaderProvider) {

    return new CdcPipelineFactory<>("eventuate-local",
            binlogEntryReaderProvider,
            new CdcDataPublisher<>(dataProducerFactory, duplicatePublishingDetector, new PublishedEventPublishingStrategy()),
            new BinlogEntryToPublishedEventConverter());
  }
}
