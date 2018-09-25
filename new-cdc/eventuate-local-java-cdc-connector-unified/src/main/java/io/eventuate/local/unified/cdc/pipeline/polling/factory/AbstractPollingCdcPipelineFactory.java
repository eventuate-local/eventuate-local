package io.eventuate.local.unified.cdc.pipeline.polling.factory;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.polling.PollingCdcDataPublisher;
import io.eventuate.local.polling.PollingDao;
import io.eventuate.local.polling.PollingDataProvider;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

public abstract class AbstractPollingCdcPipelineFactory<EVENT extends BinLogEvent> extends CommonCdcPipelineFactory<EVENT> {

  public AbstractPollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                           DataProducerFactory dataProducerFactory,
                                           BinlogEntryReaderProvider binlogEntryReaderProvider) {

    super(curatorFramework, dataProducerFactory, binlogEntryReaderProvider);
  }

  @Override
  public CdcPipeline<EVENT> create(CdcPipelineProperties cdcPipelineProperties) {
    PollingDao pollingDao = binlogEntryReaderProvider.getReader(cdcPipelineProperties.getReader());

    CdcDataPublisher<EVENT> cdcDataPublisher =
            new PollingCdcDataPublisher<>(dataProducerFactory, createPublishingStrategy());

    pollingDao.addPollingEntryHandler(createEventuateSchema(cdcPipelineProperties),
            createSourceTableNameSupplier(cdcPipelineProperties).getSourceTableName(),
            createPollingDataProvider(),
            createBinlogEntryToEventConverter(),
            cdcDataPublisher);

    return new CdcPipeline<>(cdcDataPublisher);
  }

  protected abstract PollingDataProvider createPollingDataProvider();
}
