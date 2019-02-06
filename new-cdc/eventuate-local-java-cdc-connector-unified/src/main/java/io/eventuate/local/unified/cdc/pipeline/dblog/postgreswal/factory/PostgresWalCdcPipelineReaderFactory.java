package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory;

import io.eventuate.local.common.CdcDataPublisherFactory;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.postgres.wal.PostgresWalClient;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.CommonDbLogCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;

public class PostgresWalCdcPipelineReaderFactory
        extends CommonDbLogCdcPipelineReaderFactory<PostgresWalCdcPipelineReaderProperties, PostgresWalClient> {

  public static final String TYPE = "postgres-wal";

  public PostgresWalCdcPipelineReaderFactory(DataProducerFactory dataProducerFactory,
                                             CdcDataPublisherFactory cdcDataPublisherFactory,
                                             MeterRegistry meterRegistry,
                                             CuratorFramework curatorFramework,
                                             BinlogEntryReaderProvider binlogEntryReaderProvider,
                                             EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                             EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    super(dataProducerFactory,
            cdcDataPublisherFactory,
            meterRegistry,
            curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }

  @Override
  public Class<PostgresWalCdcPipelineReaderProperties> propertyClass() {
    return PostgresWalCdcPipelineReaderProperties.class;
  }

  @Override
  public PostgresWalClient create(PostgresWalCdcPipelineReaderProperties readerProperties) {

    DataSource dataSource = createDataSource(readerProperties);

    return new PostgresWalClient(cdcDataPublisherFactory.create(dataProducerFactory.create()),
            meterRegistry,
            readerProperties.getDataSourceUrl(),
            readerProperties.getDataSourceUserName(),
            readerProperties.getDataSourcePassword(),
            readerProperties.getPostgresWalIntervalInMilliseconds(),
            readerProperties.getBinlogConnectionTimeoutInMilliseconds(),
            readerProperties.getMaxAttemptsForBinlogConnection(),
            readerProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
            readerProperties.getPostgresReplicationSlotName(),
            curatorFramework,
            readerProperties.getLeadershipLockPath(),
            dataSource,
            readerProperties.getBinlogClientId(),
            readerProperties.getReplicationLagMeasuringIntervalInMilliseconds(),
            readerProperties.getMonitoringRetryIntervalInMilliseconds(),
            readerProperties.getMonitoringRetryAttempts(),
            readerProperties.getAdditionalServiceReplicationSlotName(),
            readerProperties.getWaitForOffsetSyncTimeoutInMilliseconds());
  }
}
