package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration;

import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.pipeline.common.DefaultPipelineTypeSupplier;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory.DefaultPostgresWalCdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory.PostgresWalCdcPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PostgresWalCdcPipelineFactoryConfiguration {
  @Profile("PostgresWal")
  @Bean
  public DefaultPipelineTypeSupplier defaultPipelineTypeSupplier() {
    return () -> DefaultPostgresWalCdcPipelineFactory.TYPE;
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
  public DefaultPostgresWalCdcPipelineFactory defaultPostgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                                   DataProducerFactory dataProducerFactory,
                                                                                   EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                                   EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                                   EventuateKafkaProducer eventuateKafkaProducer,
                                                                                   PublishingFilter publishingFilter) {

    return new DefaultPostgresWalCdcPipelineFactory(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter);
  }
}
