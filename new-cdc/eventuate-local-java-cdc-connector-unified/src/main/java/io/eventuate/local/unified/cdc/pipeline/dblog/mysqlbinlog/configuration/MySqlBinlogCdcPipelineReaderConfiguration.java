package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration;

import io.eventuate.local.common.MySqlBinlogCondition;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.configuration.CommonDbLogCdcDefaultPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.OffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.DebeziumOffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.MySqlBinlogCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySqlBinlogCdcPipelineReaderConfiguration extends CommonDbLogCdcDefaultPipelineReaderConfiguration {

  @Bean("eventuateLocalMySqlBinlogCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory mySqlBinlogCdcPipelineReaderFactory(MeterRegistry meterRegistry,
                                                                      CuratorFramework curatorFramework,
                                                                      BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                                      EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                      EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                      EventuateKafkaProducer eventuateKafkaProducer,
                                                                      OffsetStoreFactory offsetStoreFactory,
                                                                      DebeziumOffsetStoreFactory debeziumOffsetStoreFactory) {

    return new MySqlBinlogCdcPipelineReaderFactory(meterRegistry,
            curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            offsetStoreFactory,
            debeziumOffsetStoreFactory);
  }

  @Conditional(MySqlBinlogCondition.class)
  @Bean("defaultCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory defaultMySqlBinlogCdcPipelineFactory(MeterRegistry meterRegistry,
                                                                       CuratorFramework curatorFramework,
                                                                       BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                       EventuateKafkaProducer eventuateKafkaProducer,
                                                                       OffsetStoreFactory offsetStoreFactory,
                                                                       DebeziumOffsetStoreFactory debeziumOffsetStoreFactory) {

    return new MySqlBinlogCdcPipelineReaderFactory(meterRegistry,
            curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            offsetStoreFactory,
            debeziumOffsetStoreFactory);
  }

  @Conditional(MySqlBinlogCondition.class)
  @Bean
  public CdcPipelineReaderProperties defaultMySqlPipelineReaderProperties() {
    MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties = createMySqlBinlogCdcPipelineReaderProperties();

    mySqlBinlogCdcPipelineReaderProperties.setType(MySqlBinlogCdcPipelineReaderFactory.TYPE);

    initCommonDbLogCdcPipelineReaderProperties(mySqlBinlogCdcPipelineReaderProperties);
    initCdcPipelineReaderProperties(mySqlBinlogCdcPipelineReaderProperties);

    mySqlBinlogCdcPipelineReaderProperties.setMySqlBinLogClientName(eventuateConfigurationProperties.getMySqlBinLogClientName());

    return mySqlBinlogCdcPipelineReaderProperties;
  }

  private MySqlBinlogCdcPipelineReaderProperties createMySqlBinlogCdcPipelineReaderProperties() {
    MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties = new MySqlBinlogCdcPipelineReaderProperties();

    mySqlBinlogCdcPipelineReaderProperties.setCdcDbUserName(eventuateConfigurationProperties.getDbUserName());
    mySqlBinlogCdcPipelineReaderProperties.setCdcDbPassword(eventuateConfigurationProperties.getDbPassword());
    mySqlBinlogCdcPipelineReaderProperties.setOldDebeziumDbOffsetStorageTopicName(eventuateConfigurationProperties.getOldDebeziumDbOffsetStorageTopicName());

    return mySqlBinlogCdcPipelineReaderProperties;
  }
}
