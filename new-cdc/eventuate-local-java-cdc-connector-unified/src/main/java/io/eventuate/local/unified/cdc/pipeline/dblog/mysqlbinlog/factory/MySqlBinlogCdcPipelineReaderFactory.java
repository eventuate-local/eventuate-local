package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.DebeziumBinlogOffsetKafkaStore;
import io.eventuate.local.mysql.binlog.MySqlBinaryLogClient;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.CommonDbLogCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.OffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineReaderProperties;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;

public class MySqlBinlogCdcPipelineReaderFactory extends CommonDbLogCdcPipelineReaderFactory<MySqlBinlogCdcPipelineReaderProperties, MySqlBinaryLogClient> {
  public static final String TYPE = "mysql-binlog";

  public MySqlBinlogCdcPipelineReaderFactory(CuratorFramework curatorFramework,
                                             BinlogEntryReaderProvider binlogEntryReaderProvider,
                                             EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                             EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                             EventuateKafkaProducer eventuateKafkaProducer,
                                             OffsetStoreFactory offsetStoreFactory) {

    super(curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            offsetStoreFactory);
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }

  @Override
  public Class<MySqlBinlogCdcPipelineReaderProperties> propertyClass() {
    return MySqlBinlogCdcPipelineReaderProperties.class;
  }

  @Override
  public MySqlBinaryLogClient create(MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties) {
    DataSource dataSource = createDataSource(mySqlBinlogCdcPipelineReaderProperties);

    return new MySqlBinaryLogClient(mySqlBinlogCdcPipelineReaderProperties.getCdcDbUserName(),
            mySqlBinlogCdcPipelineReaderProperties.getCdcDbPassword(),
            mySqlBinlogCdcPipelineReaderProperties.getDataSourceUrl(),
            createDataSource(mySqlBinlogCdcPipelineReaderProperties),
            mySqlBinlogCdcPipelineReaderProperties.getBinlogClientId(),
            mySqlBinlogCdcPipelineReaderProperties.getMySqlBinLogClientName(),
            mySqlBinlogCdcPipelineReaderProperties.getBinlogConnectionTimeoutInMilliseconds(),
            mySqlBinlogCdcPipelineReaderProperties.getMaxAttemptsForBinlogConnection(),
            curatorFramework,
            mySqlBinlogCdcPipelineReaderProperties.getLeadershipLockPath(),
            offsetStoreFactory.create(mySqlBinlogCdcPipelineReaderProperties,
                    dataSource,
                    new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA),
                    mySqlBinlogCdcPipelineReaderProperties.getMySqlBinLogClientName()),
            createDebeziumBinlogOffsetKafkaStore(mySqlBinlogCdcPipelineReaderProperties,
                    eventuateKafkaConfigurationProperties,
                    eventuateKafkaConsumerConfigurationProperties));
  }

  protected DebeziumBinlogOffsetKafkaStore createDebeziumBinlogOffsetKafkaStore(MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties,
                                                                                EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                                EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return new DebeziumBinlogOffsetKafkaStore(mySqlBinlogCdcPipelineReaderProperties.getOldDbHistoryTopicName(),
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }
}
