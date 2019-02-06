package io.eventuate.local.unified.cdc.pipeline.common.factory;

import com.zaxxer.hikari.HikariDataSource;
import io.eventuate.local.common.BinlogEntryReader;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.CdcDataPublisherFactory;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;

abstract public class CommonCdcPipelineReaderFactory<PROPERTIES extends CdcPipelineReaderProperties, READER extends BinlogEntryReader>
        implements CdcPipelineReaderFactory<PROPERTIES, READER> {

  protected DataProducerFactory dataProducerFactory;
  protected CdcDataPublisherFactory cdcDataPublisherFactory;
  protected MeterRegistry meterRegistry;
  protected CuratorFramework curatorFramework;
  protected BinlogEntryReaderProvider binlogEntryReaderProvider;


  public CommonCdcPipelineReaderFactory(DataProducerFactory dataProducerFactory,
                                        CdcDataPublisherFactory cdcDataPublisherFactory,
                                        MeterRegistry meterRegistry,
                                        CuratorFramework curatorFramework,
                                        BinlogEntryReaderProvider binlogEntryReaderProvider) {

    this.dataProducerFactory = dataProducerFactory;
    this.cdcDataPublisherFactory = cdcDataPublisherFactory;
    this.meterRegistry = meterRegistry;
    this.curatorFramework = curatorFramework;
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
