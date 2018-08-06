package io.eventuate.local.unified.cdc.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.unified.cdc.properties.CdcPipelineProperties;
import io.eventuate.local.unified.cdc.properties.MySqlBinlogCdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

abstract public class CommonCdcPipelineFactory<PROPERTIES extends CdcPipelineProperties> implements CdcPipelineFactory<PROPERTIES> {
  protected CuratorFramework curatorFramework;

  public CommonCdcPipelineFactory(CuratorFramework curatorFramework) {
    this.curatorFramework = curatorFramework;
  }

  protected EventTableChangesToAggregateTopicTranslator<PublishedEvent> createEventTableChangesToAggregateTopicTranslator(PROPERTIES properties,
                                                                                                                          CdcDataPublisher<PublishedEvent> cdcDataPublisher,
                                                                                                                          CdcProcessor<PublishedEvent> cdcProcessor) {


    return new EventTableChangesToAggregateTopicTranslator<>(cdcDataPublisher,
            cdcProcessor,
            curatorFramework,
            properties.getLeadershipLockPath());
  }

  protected EventuateSchema createEventuateSchema(MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties) {
    return new EventuateSchema(mySqlBinlogCdcPipelineProperties.getEventuateDatabaseSchema());
  }
}
