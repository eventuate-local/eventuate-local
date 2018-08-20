package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory;

import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.curator.framework.CuratorFramework;

public class DefaultMySqlBinlogCdcPipelineFactory extends MySqlBinlogCdcPipelineFactory {

  public static final String TYPE = "default-eventuate-local-mysql-binlog";

  public DefaultMySqlBinlogCdcPipelineFactory(CuratorFramework curatorFramework,
                                              DataProducerFactory dataProducerFactory,
                                              EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                              EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                              EventuateKafkaProducer eventuateKafkaProducer,
                                              PublishingFilter publishingFilter) {
    super(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter);
  }


  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }
}
