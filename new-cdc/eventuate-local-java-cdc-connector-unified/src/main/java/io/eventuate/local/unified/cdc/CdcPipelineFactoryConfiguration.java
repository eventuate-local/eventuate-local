package io.eventuate.local.unified.cdc;

import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.factory.MySqlBinlogCdcPipelineFactory;
import io.eventuate.local.unified.cdc.factory.PollingCdcPipelineFactory;
import io.eventuate.local.unified.cdc.factory.PostgresWalCdcPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CdcPipelineFactoryConfiguration {

  @Bean
  public MySqlBinlogCdcPipelineFactory mySqlBinlogCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                     DataProducerFactory dataProducerFactory,
                                                                     EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                     EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                     EventuateKafkaProducer eventuateKafkaProducer,
                                                                     PublishingFilter publishingFilter) {

    return new MySqlBinlogCdcPipelineFactory(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter);
  }

  @Bean
  public PostgresWalCdcPipelineFactory postgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                     DataProducerFactory dataProducerFactory,
                                                                     EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                     EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                     EventuateKafkaProducer eventuateKafkaProducer,
                                                                     PublishingFilter publishingFilter) {

    return new PostgresWalCdcPipelineFactory(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter);
  }

  @Bean
  public PollingCdcPipelineFactory pollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                                             DataProducerFactory dataProducerFactory) {

    return new PollingCdcPipelineFactory(curatorFramework, dataProducerFactory);
  }
}
