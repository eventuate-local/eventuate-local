package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration;

import io.eventuate.local.common.CdcDataPublisherFactory;
import io.eventuate.local.common.MySqlBinlogCondition;
import io.eventuate.local.java.common.broker.CdcDataPublisherTransactionTemplateFactory;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
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
  public CdcPipelineReaderFactory mySqlBinlogCdcPipelineReaderFactory(DataProducerFactory dataProducerFactory,
                                                                      CdcDataPublisherFactory cdcDataPublisherFactory,
                                                                      CdcDataPublisherTransactionTemplateFactory cdcDataPublisherTransactionTemplateFactory,
                                                                      MeterRegistry meterRegistry,
                                                                      CuratorFramework curatorFramework,
                                                                      BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                                      EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                      EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                      OffsetStoreFactory offsetStoreFactory,
                                                                      DebeziumOffsetStoreFactory debeziumOffsetStoreFactory) {

    return new MySqlBinlogCdcPipelineReaderFactory(dataProducerFactory,
            cdcDataPublisherFactory,
            cdcDataPublisherTransactionTemplateFactory,
            meterRegistry,
            curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            offsetStoreFactory,
            debeziumOffsetStoreFactory);
  }

  @Conditional(MySqlBinlogCondition.class)
  @Bean("defaultCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory defaultMySqlBinlogCdcPipelineFactory(DataProducerFactory dataProducerFactory,
                                                                       CdcDataPublisherFactory cdcDataPublisherFactory,
                                                                       CdcDataPublisherTransactionTemplateFactory cdcDataPublisherTransactionTemplateFactory,
                                                                       MeterRegistry meterRegistry,
                                                                       CuratorFramework curatorFramework,
                                                                       BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                       OffsetStoreFactory offsetStoreFactory,
                                                                       DebeziumOffsetStoreFactory debeziumOffsetStoreFactory) {

    return new MySqlBinlogCdcPipelineReaderFactory(dataProducerFactory,
            cdcDataPublisherFactory,
            cdcDataPublisherTransactionTemplateFactory,
            meterRegistry,
            curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
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

    mySqlBinlogCdcPipelineReaderProperties.setMySqlBinlogClientName(eventuateConfigurationProperties.getMySqlBinlogClientName());

    return mySqlBinlogCdcPipelineReaderProperties;
  }

  private MySqlBinlogCdcPipelineReaderProperties createMySqlBinlogCdcPipelineReaderProperties() {
    MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties = new MySqlBinlogCdcPipelineReaderProperties();

    mySqlBinlogCdcPipelineReaderProperties.setCdcDbUserName(eventuateConfigurationProperties.getDbUserName());
    mySqlBinlogCdcPipelineReaderProperties.setCdcDbPassword(eventuateConfigurationProperties.getDbPassword());
    mySqlBinlogCdcPipelineReaderProperties.setReadOldDebeziumDbOffsetStorageTopic(eventuateConfigurationProperties.getReadOldDebeziumDbOffsetStorageTopic());

    return mySqlBinlogCdcPipelineReaderProperties;
  }
}
