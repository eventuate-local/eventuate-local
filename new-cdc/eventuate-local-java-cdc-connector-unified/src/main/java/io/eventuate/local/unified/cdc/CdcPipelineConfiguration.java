package io.eventuate.local.unified.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.eventuate.local.common.EventTableChangesToAggregateTopicTranslatorConfiguration;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.PublishingStrategy;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

@Configuration
@Import(EventTableChangesToAggregateTopicTranslatorConfiguration.class)
@EnableConfigurationProperties(EventuateKafkaConsumerConfigurationProperties.class)
public class CdcPipelineConfiguration {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private ObjectMapper objectMapper = new ObjectMapper();
  private List<CdcPipeline> cdcPipelines = new ArrayList<>();

  @Value("${eventuate.cdc.pipeline.properties:#{null}}")
  private String cdcPipelineJsonProperties;

  @Autowired
  private Collection<CdcPipelineFactory> cdcPipelineFactories;

  @PostConstruct
  public void initialize() {
    logger.info("Starting unified cdc pipelines");

    Optional
            .ofNullable(cdcPipelineJsonProperties)
            .map(this::convertCdcPipelinePropertiesToListOfMaps)
            .orElse(Collections.emptyList())
            .forEach(properties -> createStartSaveCdcPipeline(cdcPipelineFactories, properties));

    logger.info("Unified cdc pipelines are started");
  }

  @PreDestroy
  public void stop() {
    cdcPipelines.forEach(CdcPipeline::stop);
  }

  @Bean
  public MySqlBinlogCdcPipelineFactory mySqlBinlogCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                     DataProducerFactory dataProducerFactory,
                                                                     EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                     EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                     EventuateKafkaProducer eventuateKafkaProducer,
                                                                     PublishingStrategy<PublishedEvent> publishingStrategy) {

    return new MySqlBinlogCdcPipelineFactory(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingStrategy);
  }

  @Bean
  public PostgresWalCdcPipelineFactory postgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                     PublishingStrategy<PublishedEvent> publishingStrategy,
                                                                     DataProducerFactory dataProducerFactory,
                                                                     EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                     EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                     EventuateKafkaProducer eventuateKafkaProducer) {

    return new PostgresWalCdcPipelineFactory(curatorFramework,
            publishingStrategy,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer);
  }

  @Bean
  public PollingCdcPipelineFactory pollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                                             PublishingStrategy<PublishedEvent> publishingStrategy,
                                                             DataProducerFactory dataProducerFactory) {

    return new PollingCdcPipelineFactory(curatorFramework, publishingStrategy, dataProducerFactory);
  }

  private List<Map<String, Object>> convertCdcPipelinePropertiesToListOfMaps(String properties) {
    try {
      return objectMapper.readValue(properties, List.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void createStartSaveCdcPipeline(Collection<CdcPipelineFactory> cdcPipelineFactories,
                                          Map<String, Object> properties) {

    CdcPipeline cdcPipeline = createCdcPipeline(cdcPipelineFactories, properties);
    cdcPipeline.start();
    cdcPipelines.add(cdcPipeline);
  }

  private CdcPipeline createCdcPipeline(Collection<CdcPipelineFactory> cdcPipelineFactories,
                                        Map<String, Object> properties) {

    CdcPipelineProperties cdcPipelineProperties = objectMapper.convertValue(properties, CdcPipelineProperties.class);
    cdcPipelineProperties.validate();

    CdcPipelineFactory<? extends CdcPipelineProperties> cdcPipelineFactory = findCdcPipelineFactory(cdcPipelineFactories, cdcPipelineProperties.getType());

    CdcPipelineProperties exactCdcPipelineProperties = objectMapper.convertValue(properties, cdcPipelineFactory.propertyClass());
    exactCdcPipelineProperties.validate();

    return  ((CdcPipelineFactory)cdcPipelineFactory).create(exactCdcPipelineProperties);
  }

  private CdcPipelineFactory<? extends CdcPipelineProperties> findCdcPipelineFactory(Collection<CdcPipelineFactory> cdcPipelineFactories,
                                                                                     String type) {
    return cdcPipelineFactories
            .stream()
            .filter(factory ->  factory.supports(type))
            .findAny()
            .orElseThrow(() ->
                    new RuntimeException(String.format("factory not found for type %s",
                            type)));
  }
}
