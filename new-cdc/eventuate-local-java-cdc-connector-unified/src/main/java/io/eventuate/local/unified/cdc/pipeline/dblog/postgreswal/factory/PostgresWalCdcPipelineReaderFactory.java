package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory;

import io.eventuate.local.postgres.wal.PostgresWalClient;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineReaderProperties;
import org.apache.curator.framework.CuratorFramework;

public class PostgresWalCdcPipelineReaderFactory extends CommonCdcPipelineReaderFactory<PostgresWalCdcPipelineReaderProperties, PostgresWalClient> {
  public static final String TYPE = "postgres-wal";

  public PostgresWalCdcPipelineReaderFactory(CuratorFramework curatorFramework,
                                             BinlogEntryReaderProvider binlogEntryReaderProvider) {
    super(curatorFramework, binlogEntryReaderProvider);
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

    return new PostgresWalClient(postgresWalCdcPipelineReaderProperties.getDataSourceUrl(),
            postgresWalCdcPipelineReaderProperties.getDataSourceUserName(),
            postgresWalCdcPipelineReaderProperties.getDataSourcePassword(),
            createDataSource(postgresWalCdcPipelineReaderProperties),
            postgresWalCdcPipelineReaderProperties.getMySqlBinLogClientName(),
            postgresWalCdcPipelineReaderProperties.getBinlogConnectionTimeoutInMilliseconds(),
            postgresWalCdcPipelineReaderProperties.getMaxAttemptsForBinlogConnection(),
            postgresWalCdcPipelineReaderProperties.getPostgresWalIntervalInMilliseconds(),
            postgresWalCdcPipelineReaderProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
            postgresWalCdcPipelineReaderProperties.getPostgresReplicationSlotName(),
            curatorFramework,
            postgresWalCdcPipelineReaderProperties.getLeadershipLockPath());
  }
}
