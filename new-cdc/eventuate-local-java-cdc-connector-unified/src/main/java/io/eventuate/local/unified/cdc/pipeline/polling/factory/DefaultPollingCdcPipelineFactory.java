package io.eventuate.local.unified.cdc.pipeline.polling.factory;

import io.eventuate.local.java.common.broker.DataProducerFactory;
import org.apache.curator.framework.CuratorFramework;

public class DefaultPollingCdcPipelineFactory extends PollingCdcPipelineFactory{

  public static final String TYPE = "default-eventuate-local-event-polling";

  public DefaultPollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                          DataProducerFactory dataProducerFactory) {

    super(curatorFramework, dataProducerFactory);
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }
}
