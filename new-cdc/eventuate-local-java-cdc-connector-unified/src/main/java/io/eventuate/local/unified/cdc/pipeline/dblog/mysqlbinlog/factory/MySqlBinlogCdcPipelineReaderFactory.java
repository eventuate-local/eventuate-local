package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.CdcDataPublisherFactory;
import io.eventuate.local.java.common.broker.CdcDataPublisherTransactionTemplateFactory;
import io.eventuate.local.java.common.broker.DataProducer;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.mysql.binlog.DebeziumBinlogOffsetKafkaStore;
import io.eventuate.local.mysql.binlog.MySqlBinaryLogClient;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.CommonDbLogCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.OffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;
import java.util.Optional;

public class MySqlBinlogCdcPipelineReaderFactory extends CommonDbLogCdcPipelineReaderFactory<MySqlBinlogCdcPipelineReaderProperties, MySqlBinaryLogClient> {
  public static final String TYPE = "mysql-binlog";

  private DebeziumOffsetStoreFactory debeziumOffsetStoreFactory;
  private OffsetStoreFactory offsetStoreFactory;
  private CdcDataPublisherTransactionTemplateFactory cdcDataPublisherTransactionTemplateFactory;

  public MySqlBinlogCdcPipelineReaderFactory(DataProducerFactory dataProducerFactory,
                                             CdcDataPublisherFactory cdcDataPublisherFactory,
                                             CdcDataPublisherTransactionTemplateFactory cdcDataPublisherTransactionTemplateFactory,
                                             MeterRegistry meterRegistry,
                                             CuratorFramework curatorFramework,
                                             BinlogEntryReaderProvider binlogEntryReaderProvider,
                                             EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                             EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                             OffsetStoreFactory offsetStoreFactory,
                                             DebeziumOffsetStoreFactory debeziumOffsetStoreFactory) {

    super(dataProducerFactory,
            cdcDataPublisherFactory,
            meterRegistry,
            curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);

    this.debeziumOffsetStoreFactory = debeziumOffsetStoreFactory;
    this.offsetStoreFactory = offsetStoreFactory;
    this.cdcDataPublisherTransactionTemplateFactory = cdcDataPublisherTransactionTemplateFactory;
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
            readerProperties.getReadOldDebeziumDbOffsetStorageTopic()
                    ? Optional.of(debeziumOffsetStoreFactory.create())
                    : Optional.empty();


    return new MySqlBinaryLogClient(dataProducerFactory,
            cdcDataPublisherFactory,
            cdcDataPublisherTransactionTemplateFactory,
            meterRegistry,
            readerProperties.getCdcDbUserName(),
            readerProperties.getCdcDbPassword(),
            readerProperties.getDataSourceUrl(),
            createDataSource(readerProperties),
            readerProperties.getBinlogClientId(),
            readerProperties.getMySqlBinlogClientName(),
            readerProperties.getBinlogConnectionTimeoutInMilliseconds(),
            readerProperties.getMaxAttemptsForBinlogConnection(),
            curatorFramework,
            readerProperties.getLeadershipLockPath(),
            dataProducer -> offsetStoreFactory.create(readerProperties,
                    dataSource,
                    new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA),
                    readerProperties.getMySqlBinlogClientName(),
                    dataProducer),
            debeziumBinlogOffsetKafkaStore,
            readerProperties.getReplicationLagMeasuringIntervalInMilliseconds(),
            readerProperties.getMonitoringRetryIntervalInMilliseconds(),
            readerProperties.getMonitoringRetryAttempts(),
            readerProperties.isUseGTIDsWhenPossible());
  }
}
