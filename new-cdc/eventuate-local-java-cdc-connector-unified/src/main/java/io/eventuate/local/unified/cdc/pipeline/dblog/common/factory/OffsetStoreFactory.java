package io.eventuate.local.unified.cdc.pipeline.dblog.common.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.properties.CommonDbLogCdcPipelineReaderProperties;

import javax.sql.DataSource;

public interface OffsetStoreFactory {
  OffsetStore create(CommonDbLogCdcPipelineReaderProperties properties,
                     DataSource dataSource,
                     EventuateSchema eventuateSchema,
                     String clientName);
}
