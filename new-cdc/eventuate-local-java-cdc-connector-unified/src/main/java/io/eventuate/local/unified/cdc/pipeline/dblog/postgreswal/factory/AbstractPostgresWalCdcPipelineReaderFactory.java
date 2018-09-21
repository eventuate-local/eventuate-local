package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.postgres.wal.PostgresWalClient;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.CommonDbLogCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineReaderProperties;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;

public abstract class AbstractPostgresWalCdcPipelineReaderFactory
        extends CommonDbLogCdcPipelineReaderFactory<PostgresWalCdcPipelineReaderProperties, PostgresWalClient> {

  public static final String TYPE = "postgres-wal";

  public AbstractPostgresWalCdcPipelineReaderFactory(CuratorFramework curatorFramework,
                                                     BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                     EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                     EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                     EventuateKafkaProducer eventuateKafkaProducer) {

    super(curatorFramework,
            binlogEntryReaderProvider,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer);
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
  public PostgresWalClient create(PostgresWalCdcPipelineReaderProperties postgresWalCdcPipelineReaderProperties) {

    DataSource dataSource = createDataSource(postgresWalCdcPipelineReaderProperties);

    return new PostgresWalClient(postgresWalCdcPipelineReaderProperties.getDataSourceUrl(),
            postgresWalCdcPipelineReaderProperties.getDataSourceUserName(),
            postgresWalCdcPipelineReaderProperties.getDataSourcePassword(),
            dataSource,
            postgresWalCdcPipelineReaderProperties.getMySqlBinLogClientName(),
            postgresWalCdcPipelineReaderProperties.getBinlogConnectionTimeoutInMilliseconds(),
            postgresWalCdcPipelineReaderProperties.getMaxAttemptsForBinlogConnection(),
            postgresWalCdcPipelineReaderProperties.getPostgresWalIntervalInMilliseconds(),
            postgresWalCdcPipelineReaderProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
            postgresWalCdcPipelineReaderProperties.getPostgresReplicationSlotName(),
            curatorFramework,
            postgresWalCdcPipelineReaderProperties.getLeadershipLockPath(),
            createOffsetStore(postgresWalCdcPipelineReaderProperties,
                    dataSource,
                    new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA),
                    postgresWalCdcPipelineReaderProperties.getMySqlBinLogClientName()));
  }
}
