package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration;

import io.eventuate.local.common.MySqlBinlogCondition;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.MySqlBinlogCdcPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySqlBinlogDefaultCdcPipelineFactoryConfiguration {
  @Conditional(MySqlBinlogCondition.class)
  @Bean("defaultCdcPipelineFactory")
  public CdcPipelineFactory defaultMySqlBinlogCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                 DataProducerFactory dataProducerFactory,
                                                                 EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                 EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                 EventuateKafkaProducer eventuateKafkaProducer,
                                                                 PublishingFilter publishingFilter,
                                                                 BinlogEntryReaderProvider binlogEntryReaderProvider) {

    return new MySqlBinlogCdcPipelineFactory(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter,
            binlogEntryReaderProvider);
  }
}
