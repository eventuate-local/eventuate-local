package io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.factory;

import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.local.postgres.wal.PostgresWalClient;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;

import javax.sql.DataSource;

public class PostgresWalCdcPipelineReaderFactory
        extends CommonCdcPipelineReaderFactory<PostgresWalCdcPipelineReaderProperties, PostgresWalClient> {

  public static final String TYPE = "postgres-wal";

  public PostgresWalCdcPipelineReaderFactory(MeterRegistry meterRegistry,
                                             LeaderSelectorFactory leaderSelectorFactory,
                                             BinlogEntryReaderProvider binlogEntryReaderProvider) {

    super(meterRegistry,
            leaderSelectorFactory,
            binlogEntryReaderProvider);
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
            readerProperties.getPostgresWalIntervalInMilliseconds(),
            readerProperties.getBinlogConnectionTimeoutInMilliseconds(),
            readerProperties.getMaxAttemptsForBinlogConnection(),
            readerProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
            readerProperties.getPostgresReplicationSlotName(),
            readerProperties.getLeadershipLockPath(),
            leaderSelectorFactory,
            dataSource,
            readerProperties.getReaderName(),
            readerProperties.getReplicationLagMeasuringIntervalInMilliseconds(),
            readerProperties.getMonitoringRetryIntervalInMilliseconds(),
            readerProperties.getMonitoringRetryAttempts(),
            readerProperties.getAdditionalServiceReplicationSlotName(),
            readerProperties.getWaitForOffsetSyncTimeoutInMilliseconds());
  }
}
