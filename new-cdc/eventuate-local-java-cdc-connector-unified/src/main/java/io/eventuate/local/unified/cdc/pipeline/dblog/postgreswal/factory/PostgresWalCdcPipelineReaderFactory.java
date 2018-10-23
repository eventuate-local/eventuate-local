package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.postgres.wal.PostgresWalClient;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.CommonDbLogCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.OffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;

public class PostgresWalCdcPipelineReaderFactory
        extends CommonDbLogCdcPipelineReaderFactory<PostgresWalCdcPipelineReaderProperties, PostgresWalClient> {

  public static final String TYPE = "postgres-wal";

  public PostgresWalCdcPipelineReaderFactory(MeterRegistry meterRegistry,
                                             CuratorFramework curatorFramework,
                                             BinlogEntryReaderProvider binlogEntryReaderProvider,
                                             EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                             EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                             EventuateKafkaProducer eventuateKafkaProducer,
                                             OffsetStoreFactory offsetStoreFactory) {

    super(meterRegistry,
            curatorFramework,
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
  public Class<PostgresWalCdcPipelineReaderProperties> propertyClass() {
    return PostgresWalCdcPipelineReaderProperties.class;
  }

  @Override
  public PostgresWalClient create(PostgresWalCdcPipelineReaderProperties readerProperties) {

    DataSource dataSource = createDataSource(readerProperties);

    return new PostgresWalClient(meterRegistry,
            readerProperties.getDataSourceUrl(),
            readerProperties.getDataSourceUserName(),
            readerProperties.getDataSourcePassword(),
            readerProperties.getBinlogConnectionTimeoutInMilliseconds(),
            readerProperties.getMaxAttemptsForBinlogConnection(),
            readerProperties.getPostgresWalIntervalInMilliseconds(),
            readerProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
            readerProperties.getPostgresReplicationSlotName(),
            curatorFramework,
            readerProperties.getLeadershipLockPath(),
            offsetStoreFactory.create(readerProperties,
                    dataSource,
                    new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA),
                    readerProperties.getMySqlBinLogClientName()),
            dataSource,
            readerProperties.getBinlogClientId());
  }
}
