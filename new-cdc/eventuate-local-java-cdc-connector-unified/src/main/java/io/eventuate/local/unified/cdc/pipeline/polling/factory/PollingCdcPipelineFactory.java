package io.eventuate.local.unified.cdc.pipeline.polling.factory;

import io.eventuate.local.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.polling.EventPollingDataProvider;
import io.eventuate.local.polling.PollingDataProvider;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

public class PollingCdcPipelineFactory extends AbstractPollingCdcPipelineFactory<PublishedEvent> {

  public static final String TYPE = "eventuate-local-event-polling";

  public PollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                   DataProducerFactory dataProducerFactory,
                                   BinlogEntryReaderProvider binlogEntryReaderProvider) {

    super(curatorFramework, dataProducerFactory, binlogEntryReaderProvider);
  }

  @Override
  protected SourceTableNameSupplier createSourceTableNameSupplier(CdcPipelineProperties cdcPipelineProperties) {
    return new SourceTableNameSupplier(cdcPipelineProperties.getSourceTableName(), "events");
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }

  @Override
  protected PollingDataProvider createPollingDataProvider() {
    return new EventPollingDataProvider();
  }

  @Override
  protected BinlogEntryToEventConverter<PublishedEvent> createBinlogEntryToEventConverter() {
    return new BinlogEntryToPublishedEventConverter();
  }

  @Override
  protected PublishingStrategy<PublishedEvent> createPublishingStrategy() {
    return new PublishedEventPublishingStrategy();
  }
}
