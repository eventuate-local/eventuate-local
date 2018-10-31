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
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;
import java.util.Optional;

public class MySqlBinlogCdcPipelineReaderFactory extends CommonDbLogCdcPipelineReaderFactory<MySqlBinlogCdcPipelineReaderProperties, MySqlBinaryLogClient> {
  public static final String TYPE = "mysql-binlog";

  private DebeziumOffsetStoreFactory debeziumOffsetStoreFactory;
  private OffsetStoreFactory offsetStoreFactory;

  public MySqlBinlogCdcPipelineReaderFactory(MeterRegistry meterRegistry,
                                             CuratorFramework curatorFramework,
                                             BinlogEntryReaderProvider binlogEntryReaderProvider,
                                             EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                             EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                             EventuateKafkaProducer eventuateKafkaProducer,
                                             OffsetStoreFactory offsetStoreFactory,
                                             DebeziumOffsetStoreFactory debeziumOffsetStoreFactory) {

    super(meterRegistry,
            curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer);

    this.debeziumOffsetStoreFactory = debeziumOffsetStoreFactory;
    this.offsetStoreFactory = offsetStoreFactory;
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

    Optional<DebeziumBinlogOffsetKafkaStore> debeziumBinlogOffsetKafkaStore =
            StringUtils.isEmpty(readerProperties.getOldDbHistoryTopicName())
                    ? Optional.empty()
                    : Optional.of(debeziumOffsetStoreFactory.create(readerProperties.getOldDbHistoryTopicName()));

    return new MySqlBinaryLogClient(meterRegistry,
            readerProperties.getCdcDbUserName(),
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
            debeziumBinlogOffsetKafkaStore,
            readerProperties.getReplicationLagMeasuringIntervalInMilliseconds());
  }
}
