package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration;

import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.configuration.CommonDbLogCdcDefaultPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.OffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory.PostgresWalCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineReaderProperties;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PostgresWalCdcPipelineReaderConfiguration extends CommonDbLogCdcDefaultPipelineReaderConfiguration {

  @Bean("eventuateLocalPostgresWalCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory postgresWalCdcPipelineReaderFactory(CuratorFramework curatorFramework,
                                                                      BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                                      EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                      EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                      EventuateKafkaProducer eventuateKafkaProducer,
                                                                      OffsetStoreFactory offsetStoreFactory) {

    return new PostgresWalCdcPipelineReaderFactory(curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            offsetStoreFactory);
  }

  @Profile("PostgresWal")
  @Bean("defaultCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory defaultPostgresWalCdcPipelineReaderFactory(CuratorFramework curatorFramework,
                                                                             BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                                             EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                             EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                             EventuateKafkaProducer eventuateKafkaProducer,
                                                                             OffsetStoreFactory offsetStoreFactory) {

    return new PostgresWalCdcPipelineReaderFactory(curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            offsetStoreFactory);
  }

  @Profile("PostgresWal")
  @Bean
  public CdcPipelineReaderProperties defaultPostgresWalPipelineReaderProperties() {
    PostgresWalCdcPipelineReaderProperties postgresWalCdcPipelineReaderProperties = createPostgresWalCdcPipelineReaderProperties();

    postgresWalCdcPipelineReaderProperties.setType(PostgresWalCdcPipelineReaderFactory.TYPE);


    initCommonDbLogCdcPipelineReaderProperties(postgresWalCdcPipelineReaderProperties);
    initCdcPipelineReaderProperties(postgresWalCdcPipelineReaderProperties);

    return postgresWalCdcPipelineReaderProperties;
  }

  private PostgresWalCdcPipelineReaderProperties createPostgresWalCdcPipelineReaderProperties() {
    PostgresWalCdcPipelineReaderProperties postgresWalCdcPipelineReaderProperties = new PostgresWalCdcPipelineReaderProperties();

    postgresWalCdcPipelineReaderProperties.setPostgresReplicationStatusIntervalInMilliseconds(eventuateConfigurationProperties.getPostgresReplicationStatusIntervalInMilliseconds());
    postgresWalCdcPipelineReaderProperties.setPostgresReplicationSlotName(eventuateConfigurationProperties.getPostgresReplicationSlotName());
    postgresWalCdcPipelineReaderProperties.setPostgresWalIntervalInMilliseconds(eventuateConfigurationProperties.getPostgresWalIntervalInMilliseconds());

    return postgresWalCdcPipelineReaderProperties;
  }
}
