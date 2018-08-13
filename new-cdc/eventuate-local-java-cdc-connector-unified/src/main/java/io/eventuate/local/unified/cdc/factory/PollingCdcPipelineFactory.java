package io.eventuate.local.unified.cdc.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.polling.EventPollingDataProvider;
import io.eventuate.local.polling.PollingCdcDataPublisher;
import io.eventuate.local.polling.PollingDataProvider;
import io.eventuate.local.polling.PublishedEventBean;
import org.apache.curator.framework.CuratorFramework;

public class PollingCdcPipelineFactory extends AbstractPollingCdcPipelineFactory<PublishedEvent, PublishedEventBean, String> {

  public PollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                   PublishingStrategy<PublishedEvent> publishingStrategy,
                                   DataProducerFactory dataProducerFactory) {

    super(curatorFramework, publishingStrategy, dataProducerFactory);
  }

  @Override
  protected PollingDataProvider<PublishedEventBean, PublishedEvent, String> createPollingDataProvider(EventuateSchema eventuateSchema) {
    return new EventPollingDataProvider(eventuateSchema);
  }
}
