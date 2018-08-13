package io.eventuate.local.unified.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.factory.CdcPipelineFactory;
import io.eventuate.local.unified.cdc.factory.MySqlBinlogCdcPipelineFactory;
import io.eventuate.local.unified.cdc.factory.PollingCdcPipelineFactory;
import io.eventuate.local.unified.cdc.factory.PostgresWalCdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.CdcPipeline;
import io.eventuate.local.unified.cdc.properties.CdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

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
