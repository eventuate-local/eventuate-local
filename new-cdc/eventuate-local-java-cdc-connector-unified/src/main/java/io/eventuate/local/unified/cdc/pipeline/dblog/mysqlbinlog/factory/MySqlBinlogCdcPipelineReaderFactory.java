package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory;

import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.mysql.binlog.DebeziumBinlogOffsetKafkaStore;
import io.eventuate.local.mysql.binlog.MySqlBinaryLogClient;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.OffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;

import javax.sql.DataSource;
import java.util.Optional;

public class MySqlBinlogCdcPipelineReaderFactory extends CommonCdcPipelineReaderFactory<MySqlBinlogCdcPipelineReaderProperties, MySqlBinaryLogClient> {
  public static final String TYPE = "mysql-binlog";

  private DebeziumOffsetStoreFactory debeziumOffsetStoreFactory;
  private OffsetStoreFactory offsetStoreFactory;

  public MySqlBinlogCdcPipelineReaderFactory(MeterRegistry meterRegistry,
                                             LeaderSelectorFactory leaderSelectorFactory,
                                             BinlogEntryReaderProvider binlogEntryReaderProvider,
                                             OffsetStoreFactory offsetStoreFactory,
                                             DebeziumOffsetStoreFactory debeziumOffsetStoreFactory) {

    super(meterRegistry,
            leaderSelectorFactory,
            binlogEntryReaderProvider);

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
            readerProperties.getReadOldDebeziumDbOffsetStorageTopic()
                    ? Optional.of(debeziumOffsetStoreFactory.create())
                    : Optional.empty();

    return new MySqlBinaryLogClient(meterRegistry,
            readerProperties.getCdcDbUserName(),
            readerProperties.getCdcDbPassword(),
            readerProperties.getDataSourceUrl(),
            createDataSource(readerProperties),
            readerProperties.getReaderName(),
            readerProperties.getMySqlBinlogClientUniqueId(),
            readerProperties.getBinlogConnectionTimeoutInMilliseconds(),
            readerProperties.getMaxAttemptsForBinlogConnection(),
            readerProperties.getLeadershipLockPath(),
            leaderSelectorFactory,
            offsetStoreFactory.create(readerProperties,
                    dataSource,
                    new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA),
                    readerProperties.getOffsetStoreKey()),
            debeziumBinlogOffsetKafkaStore,
            readerProperties.getReplicationLagMeasuringIntervalInMilliseconds(),
            readerProperties.getMonitoringRetryIntervalInMilliseconds(),
            readerProperties.getMonitoringRetryAttempts());
  }
}
