package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration;

import io.eventuate.local.common.MySqlBinlogCondition;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.configuration.CommonDbLogCdcDefaultPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.AbstractMySqlBinlogCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.MySqlBinlogCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineReaderProperties;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySqlBinlogDefaultCdcPipelineReaderConfiguration extends CommonDbLogCdcDefaultPipelineReaderConfiguration {

  @Bean("eventuateLocalMySqlBinlogCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory mySqlBinlogCdcPipelineReaderFactory(CuratorFramework curatorFramework,
                                                                      BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                                      EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                      EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                      EventuateKafkaProducer eventuateKafkaProducer) {

    return new MySqlBinlogCdcPipelineReaderFactory(curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer);
  }

  @Conditional(MySqlBinlogCondition.class)
  @Bean("defaultCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory defaultMySqlBinlogCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                       BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                       EventuateKafkaProducer eventuateKafkaProducer) {

    return new MySqlBinlogCdcPipelineReaderFactory(curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer);
  }

  @Conditional(MySqlBinlogCondition.class)
  @Bean
  public CdcPipelineReaderProperties defaultMySqlPipelineReaderProperties() {
    MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties = createMySqlBinlogCdcPipelineReaderProperties();

    mySqlBinlogCdcPipelineReaderProperties.setType(AbstractMySqlBinlogCdcPipelineReaderFactory.TYPE);

    initCommonDbLogCdcPipelineReaderProperties(mySqlBinlogCdcPipelineReaderProperties);
    initCdcPipelineReaderProperties(mySqlBinlogCdcPipelineReaderProperties);

    return mySqlBinlogCdcPipelineReaderProperties;
  }

  private MySqlBinlogCdcPipelineReaderProperties createMySqlBinlogCdcPipelineReaderProperties() {
    MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties = new MySqlBinlogCdcPipelineReaderProperties();

    mySqlBinlogCdcPipelineReaderProperties.setCdcDbUserName(eventuateConfigurationProperties.getDbUserName());
    mySqlBinlogCdcPipelineReaderProperties.setCdcDbPassword(eventuateConfigurationProperties.getDbPassword());
    mySqlBinlogCdcPipelineReaderProperties.setBinlogClientId(eventuateConfigurationProperties.getBinlogClientId());
    mySqlBinlogCdcPipelineReaderProperties.setOldDbHistoryTopicName(eventuateConfigurationProperties.getOldDbHistoryTopicName());

    return mySqlBinlogCdcPipelineReaderProperties;
  }
}
