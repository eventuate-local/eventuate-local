package io.eventuate.local.unified.cdc;

import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.PublishingStrategy;
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
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonCdcPipelineConfiguration.class)
public class CdcPipelineFactoryConfiguration {

  @Bean
  public MySqlBinlogCdcPipelineFactory mySqlBinlogCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                     DataProducerFactory dataProducerFactory,
                                                                     EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                     EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                     EventuateKafkaProducer eventuateKafkaProducer,
                                                                     PublishingStrategy<PublishedEvent> publishingStrategy,
                                                                     PublishingFilter publishingFilter) {

    return new MySqlBinlogCdcPipelineFactory(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingStrategy,
            publishingFilter);
  }

  @Bean
  public PostgresWalCdcPipelineFactory postgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                     PublishingStrategy<PublishedEvent> publishingStrategy,
                                                                     DataProducerFactory dataProducerFactory,
                                                                     EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                     EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                     EventuateKafkaProducer eventuateKafkaProducer,
                                                                     PublishingFilter publishingFilter) {

    return new PostgresWalCdcPipelineFactory(curatorFramework,
            publishingStrategy,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter);
  }

  @Bean
  public PollingCdcPipelineFactory pollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                                             PublishingStrategy<PublishedEvent> publishingStrategy,
                                                             DataProducerFactory dataProducerFactory) {

    return new PollingCdcPipelineFactory(curatorFramework, publishingStrategy, dataProducerFactory);
  }
}
