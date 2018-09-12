package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.*;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.CommonDBLogCdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;

import javax.sql.DataSource;

public abstract class AbstractMySqlBinlogCdcPipelineFactory<EVENT extends BinLogEvent> extends CommonDBLogCdcPipelineFactory<MySqlBinlogCdcPipelineProperties, EVENT> {

  public AbstractMySqlBinlogCdcPipelineFactory(CuratorFramework curatorFramework,
                                               DataProducerFactory dataProducerFactory,
                                               EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                               EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                               EventuateKafkaProducer eventuateKafkaProducer,
                                               PublishingFilter publishingFilter) {
    super(curatorFramework,
          dataProducerFactory,
          eventuateKafkaConfigurationProperties,
          eventuateKafkaConsumerConfigurationProperties,
          eventuateKafkaProducer,
          publishingFilter);
  }

  @Override
  public Class<MySqlBinlogCdcPipelineProperties> propertyClass() {
    return MySqlBinlogCdcPipelineProperties.class;
  }

  @Override
  public CdcPipeline<EVENT> create(MySqlBinlogCdcPipelineProperties cdcPipelineProperties) {
    DataSource dataSource = createDataSource(cdcPipelineProperties);

    EventuateSchema eventuateSchema = createEventuateSchema(cdcPipelineProperties);

    OffsetStore offsetStore = createOffsetStore(cdcPipelineProperties, dataSource, eventuateSchema);

    CdcDataPublisher<EVENT> cdcDataPublisher = createCdcDataPublisher(offsetStore);

    SourceTableNameSupplier sourceTableNameSupplier = createSourceTableNameSupplier(cdcPipelineProperties);

    DbLogClient dbLogClient = createDbLogClient(cdcPipelineProperties, sourceTableNameSupplier, dataSource, eventuateSchema);

    BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter = createBinlogEntryToEventConverter();

    DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore =
            createDebeziumBinlogOffsetKafkaStore(cdcPipelineProperties, eventuateKafkaConfigurationProperties, eventuateKafkaConsumerConfigurationProperties);

    CdcProcessor<EVENT> cdcProcessor = createCdcProcessor(dbLogClient, offsetStore, binlogEntryToEventConverter, debeziumBinlogOffsetKafkaStore);

    EventTableChangesToAggregateTopicTranslator<EVENT> publishedEventEventTableChangesToAggregateTopicTranslator =
            createEventTableChangesToAggregateTopicTranslator(cdcPipelineProperties, cdcDataPublisher, cdcProcessor);

    return new CdcPipeline<>(publishedEventEventTableChangesToAggregateTopicTranslator);
  }

  protected DbLogClient createDbLogClient(MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties,
                                         SourceTableNameSupplier sourceTableNameSupplier,
                                         DataSource dataSource,
                                         EventuateSchema eventuateSchema) {

    JdbcUrl jdbcUrl = JdbcUrlParser.parse(mySqlBinlogCdcPipelineProperties.getDataSourceUrl());

    return new MySqlBinaryLogClient(dataSource,
            eventuateSchema,
            mySqlBinlogCdcPipelineProperties.getCdcDbUserName(),
            mySqlBinlogCdcPipelineProperties.getCdcDbPassword(),
            jdbcUrl.getHost(),
            jdbcUrl.getPort(),
            mySqlBinlogCdcPipelineProperties.getBinlogClientId(),
            sourceTableNameSupplier.getSourceTableName(),
            mySqlBinlogCdcPipelineProperties.getMySqlBinLogClientName(),
            mySqlBinlogCdcPipelineProperties.getBinlogConnectionTimeoutInMilliseconds(),
            mySqlBinlogCdcPipelineProperties.getMaxAttemptsForBinlogConnection());
  }

  protected DebeziumBinlogOffsetKafkaStore createDebeziumBinlogOffsetKafkaStore(MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties,
                                                                              EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                              EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return new DebeziumBinlogOffsetKafkaStore(mySqlBinlogCdcPipelineProperties.getOldDbHistoryTopicName(),
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }

  protected CdcProcessor<EVENT> createCdcProcessor(DbLogClient dbLogClient,
                                                   OffsetStore offsetStore,
                                                   BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                                                   DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore) {

    return new MySQLCdcProcessor<>(dbLogClient, offsetStore, binlogEntryToEventConverter, debeziumBinlogOffsetKafkaStore);
  }
}
