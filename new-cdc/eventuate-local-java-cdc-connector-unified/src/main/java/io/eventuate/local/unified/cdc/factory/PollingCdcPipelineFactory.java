package io.eventuate.local.unified.cdc.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.PublishedEventPublishingStrategy;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.polling.EventPollingDataProvider;
import io.eventuate.local.polling.PollingDataProvider;
import io.eventuate.local.polling.PublishedEventBean;
import io.eventuate.local.unified.cdc.CdcPipelineType;
import org.apache.curator.framework.CuratorFramework;

public class PollingCdcPipelineFactory extends AbstractPollingCdcPipelineFactory<PublishedEvent, PublishedEventBean, String> {

  public PollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                   DataProducerFactory dataProducerFactory) {

    super(curatorFramework, dataProducerFactory);
  }

  @Override
  public boolean supports(String type) {
    return CdcPipelineType.EVENT_POLLING.stringRepresentation.equals(type);
  }

  @Override
  protected PollingDataProvider<PublishedEventBean, PublishedEvent, String> createPollingDataProvider(EventuateSchema eventuateSchema) {
    return new EventPollingDataProvider(eventuateSchema);
  }

  @Override
  protected PublishingStrategy<PublishedEvent> createPublishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }
}
