package io.eventuate.local.unified.cdc.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.*;
import io.eventuate.local.unified.cdc.CdcPipelineType;
import io.eventuate.local.unified.cdc.pipeline.CdcPipeline;
import io.eventuate.local.unified.cdc.properties.MySqlBinlogCdcPipelineProperties;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;

import javax.sql.DataSource;

public class MySqlBinlogCdcPipelineFactory extends CommonDBLogCdcPipelineFactory<MySqlBinlogCdcPipelineProperties> {

  public MySqlBinlogCdcPipelineFactory(CuratorFramework curatorFramework,
                                       DataProducerFactory dataProducerFactory,
                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                       EventuateKafkaProducer eventuateKafkaProducer,
                                       PublishingStrategy<PublishedEvent> publishingStrategy) {
    super(curatorFramework,
            publishingStrategy,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer);
  }

  @Override
  public boolean supports(String type) {
    return CdcPipelineType.MYSQL_BINLOG.stringRepresentation.equals(type);
  }

  @Override
  public Class<MySqlBinlogCdcPipelineProperties> propertyClass() {
    return MySqlBinlogCdcPipelineProperties.class;
  }

  @Override
  public CdcPipeline create(MySqlBinlogCdcPipelineProperties cdcPipelineProperties) {
    OffsetStore offsetStore = createOffsetStore(cdcPipelineProperties);

    CdcDataPublisher<PublishedEvent> cdcDataPublisher = createCdcDataPublisher(offsetStore);

    SourceTableNameSupplier sourceTableNameSupplier = createSourceTableNameSupplier(cdcPipelineProperties);

    EventuateSchema eventuateSchema = createEventuateSchema(cdcPipelineProperties);

    DataSource dataSource = createDataSource(cdcPipelineProperties);

    IWriteRowsEventDataParser<PublishedEvent> writeRowsEventDataParser = createWriteRowsEventDataParser(eventuateSchema, dataSource, sourceTableNameSupplier);

    DbLogClient<PublishedEvent> dbLogClient = createDbLogClient(cdcPipelineProperties, sourceTableNameSupplier, writeRowsEventDataParser);

    DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore =
            createDebeziumBinlogOffsetKafkaStore(cdcPipelineProperties, eventuateKafkaConfigurationProperties, eventuateKafkaConsumerConfigurationProperties);

    CdcProcessor<PublishedEvent> cdcProcessor = createCdcProcessor(dbLogClient, offsetStore, debeziumBinlogOffsetKafkaStore);

    EventTableChangesToAggregateTopicTranslator<PublishedEvent> publishedEventEventTableChangesToAggregateTopicTranslator =
            createEventTableChangesToAggregateTopicTranslator(cdcPipelineProperties, cdcDataPublisher, cdcProcessor);

    return new CdcPipeline(publishedEventEventTableChangesToAggregateTopicTranslator);
  }

  private SourceTableNameSupplier createSourceTableNameSupplier(MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties) {
    return new SourceTableNameSupplier(mySqlBinlogCdcPipelineProperties.getSourceTableName(), "EVENTS");
  }

  private IWriteRowsEventDataParser<PublishedEvent> createWriteRowsEventDataParser(EventuateSchema eventuateSchema,
                                                   DataSource dataSource,
                                                   SourceTableNameSupplier sourceTableNameSupplier) {

    return new WriteRowsEventDataParser(dataSource, sourceTableNameSupplier.getSourceTableName(), eventuateSchema);
  }

  private DbLogClient<PublishedEvent> createDbLogClient(MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties,
                                                       SourceTableNameSupplier sourceTableNameSupplier,
                                                       IWriteRowsEventDataParser<PublishedEvent> writeRowsEventDataParser) {

    JdbcUrl jdbcUrl = JdbcUrlParser.parse(mySqlBinlogCdcPipelineProperties.getDataSourceUrl());

    return new MySqlBinaryLogClient<>(writeRowsEventDataParser,
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

  private DebeziumBinlogOffsetKafkaStore createDebeziumBinlogOffsetKafkaStore(MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties,
                                                                              EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                              EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return new DebeziumBinlogOffsetKafkaStore(mySqlBinlogCdcPipelineProperties.getOldDbHistoryTopicName(),
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }

  private CdcProcessor<PublishedEvent> createCdcProcessor(DbLogClient<PublishedEvent> dbLogClient,
                                                   OffsetStore offsetStore,
                                                   DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore) {

    return new MySQLCdcProcessor<>(dbLogClient, offsetStore, debeziumBinlogOffsetKafkaStore);
  }
}
