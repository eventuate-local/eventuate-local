package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration;

import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.local.common.MySqlBinlogCondition;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.configuration.CommonDbLogCdcDefaultPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.OffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.DebeziumOffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.MySqlBinlogCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySqlBinlogCdcPipelineReaderConfiguration extends CommonDbLogCdcDefaultPipelineReaderConfiguration {

  @Bean("eventuateLocalMySqlBinlogCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory mySqlBinlogCdcPipelineReaderFactory(MeterRegistry meterRegistry,
                                                                      LeaderSelectorFactory leaderSelectorFactory,
                                                                      BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                                      OffsetStoreFactory offsetStoreFactory,
                                                                      DebeziumOffsetStoreFactory debeziumOffsetStoreFactory) {

    return new MySqlBinlogCdcPipelineReaderFactory(meterRegistry,
            leaderSelectorFactory,
            binlogEntryReaderProvider,
            offsetStoreFactory,
            debeziumOffsetStoreFactory);
  }

  @Conditional(MySqlBinlogCondition.class)
  @Bean("defaultCdcPipelineReaderFactory")
  public CdcPipelineReaderFactory defaultMySqlBinlogCdcPipelineFactory(MeterRegistry meterRegistry,
                                                                       LeaderSelectorFactory leaderSelectorFactory,
                                                                       BinlogEntryReaderProvider binlogEntryReaderProvider,
                                                                       OffsetStoreFactory offsetStoreFactory,
                                                                       DebeziumOffsetStoreFactory debeziumOffsetStoreFactory) {

    return new MySqlBinlogCdcPipelineReaderFactory(meterRegistry,
            leaderSelectorFactory,
            binlogEntryReaderProvider,
            offsetStoreFactory,
            debeziumOffsetStoreFactory);
  }

  @Conditional(MySqlBinlogCondition.class)
  @Bean
  public CdcPipelineReaderProperties defaultMySqlPipelineReaderProperties() {
    MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties = createMySqlBinlogCdcPipelineReaderProperties();

    mySqlBinlogCdcPipelineReaderProperties.setType(MySqlBinlogCdcPipelineReaderFactory.TYPE);

    initCommonDbLogCdcPipelineReaderProperties(mySqlBinlogCdcPipelineReaderProperties);
    initCdcPipelineReaderProperties(mySqlBinlogCdcPipelineReaderProperties);

    mySqlBinlogCdcPipelineReaderProperties.setMySqlBinlogClientUniqueId(eventuateConfigurationProperties.getMySqlBinlogClientUniqueId());

    return mySqlBinlogCdcPipelineReaderProperties;
  }

  private MySqlBinlogCdcPipelineReaderProperties createMySqlBinlogCdcPipelineReaderProperties() {
    MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties = new MySqlBinlogCdcPipelineReaderProperties();

    mySqlBinlogCdcPipelineReaderProperties.setCdcDbUserName(eventuateConfigurationProperties.getDbUserName());
    mySqlBinlogCdcPipelineReaderProperties.setCdcDbPassword(eventuateConfigurationProperties.getDbPassword());
    mySqlBinlogCdcPipelineReaderProperties.setReadOldDebeziumDbOffsetStorageTopic(eventuateConfigurationProperties.getReadOldDebeziumDbOffsetStorageTopic());
    mySqlBinlogCdcPipelineReaderProperties.setOffsetStoreKey(eventuateConfigurationProperties.getOffsetStoreKey());

    return mySqlBinlogCdcPipelineReaderProperties;
  }
}
