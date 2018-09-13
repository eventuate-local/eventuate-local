package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration;

import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.DbLogClientProvider;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory.PostgresWalCdcPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PostgresWalDefaultCdcPipelineFactoryConfiguration {
  @Profile("PostgresWal")
  @Bean("default")
  public CdcPipelineFactory defaultPostgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                 DataProducerFactory dataProducerFactory,
                                                                 EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                 EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                 EventuateKafkaProducer eventuateKafkaProducer,
                                                                 PublishingFilter publishingFilter,
                                                                 DbLogClientProvider dbLogClientProvider) {

    return new PostgresWalCdcPipelineFactory(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter,
            dbLogClientProvider);
  }
}
