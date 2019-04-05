package io.eventuate.local.unified.cdc.pipeline.common.factory;

import com.zaxxer.hikari.HikariDataSource;
import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.local.common.BinlogEntryReader;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;

import javax.sql.DataSource;

abstract public class CommonCdcPipelineReaderFactory<PROPERTIES extends CdcPipelineReaderProperties, READER extends BinlogEntryReader>
        implements CdcPipelineReaderFactory<PROPERTIES, READER> {

  protected MeterRegistry meterRegistry;
  protected LeaderSelectorFactory leaderSelectorFactory;
  protected BinlogEntryReaderProvider binlogEntryReaderProvider;


  public CommonCdcPipelineReaderFactory(MeterRegistry meterRegistry,
                                        LeaderSelectorFactory leaderSelectorFactory,
                                        BinlogEntryReaderProvider binlogEntryReaderProvider) {
    this.meterRegistry = meterRegistry;
    this.leaderSelectorFactory = leaderSelectorFactory;
    this.binlogEntryReaderProvider = binlogEntryReaderProvider;
  }

  public abstract READER create(PROPERTIES cdcPipelineReaderProperties);

  protected DataSource createDataSource(PROPERTIES properties) {
    HikariDataSource hikariDataSource = new HikariDataSource();
    hikariDataSource.setUsername(properties.getDataSourceUserName());
    hikariDataSource.setPassword(properties.getDataSourcePassword());
    hikariDataSource.setJdbcUrl(properties.getDataSourceUrl());
    hikariDataSource.setDriverClassName(properties.getDataSourceDriverClassName());

    hikariDataSource.setConnectionTestQuery("select 1");

    return hikariDataSource;
  }
}
