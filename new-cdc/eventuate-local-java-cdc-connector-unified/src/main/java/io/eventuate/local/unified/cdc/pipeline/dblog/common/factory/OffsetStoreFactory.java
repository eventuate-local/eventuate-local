package io.eventuate.local.unified.cdc.pipeline.dblog.common.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.OffsetStore;
import io.eventuate.local.java.common.broker.DataProducer;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.properties.CommonDbLogCdcPipelineReaderProperties;

import javax.sql.DataSource;

public interface OffsetStoreFactory {
  OffsetStore create(CommonDbLogCdcPipelineReaderProperties properties,
                     DataSource dataSource,
                     EventuateSchema eventuateSchema,
                     String clientName,
                     DataProducer dataProducer);
}
