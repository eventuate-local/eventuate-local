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

  private DebeziumOffsetStoreFactory debeziumOffsetStoreFactory;

  public MySqlBinlogCdcPipelineReaderFactory(CuratorFramework curatorFramework,
                                             BinlogEntryReaderProvider binlogEntryReaderProvider,
                                             EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                             EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                             EventuateKafkaProducer eventuateKafkaProducer,
                                             OffsetStoreFactory offsetStoreFactory,
                                             DebeziumOffsetStoreFactory debeziumOffsetStoreFactory) {

    super(curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            offsetStoreFactory);

    this.debeziumOffsetStoreFactory = debeziumOffsetStoreFactory;
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
  public MySqlBinaryLogClient create(MySqlBinlogCdcPipelineReaderProperties readerProperties) {
    DataSource dataSource = createDataSource(readerProperties);

    return new MySqlBinaryLogClient(readerProperties.getCdcDbUserName(),
            readerProperties.getCdcDbPassword(),
            readerProperties.getDataSourceUrl(),
            createDataSource(readerProperties),
            readerProperties.getBinlogClientId(),
            readerProperties.getMySqlBinLogClientName(),
            readerProperties.getBinlogConnectionTimeoutInMilliseconds(),
            readerProperties.getMaxAttemptsForBinlogConnection(),
            curatorFramework,
            readerProperties.getLeadershipLockPath(),
            offsetStoreFactory.create(readerProperties,
                    dataSource,
                    new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA),
                    readerProperties.getMySqlBinLogClientName()),
            debeziumOffsetStoreFactory.create(readerProperties.getOldDbHistoryTopicName()));
  }
}
