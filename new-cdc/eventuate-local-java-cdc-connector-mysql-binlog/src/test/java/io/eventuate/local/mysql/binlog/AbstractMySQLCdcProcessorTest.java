package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractMySQLCdcProcessorTest extends CdcProcessorTest {

  @Value("${spring.datasource.url}")
  private String dataSourceURL;

  @Autowired
  private EventuateConfigurationProperties eventuateConfigurationProperties;

  @Autowired
  private EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;

  @Autowired
  private EventuateKafkaProducer eventuateKafkaProducer;

  @Autowired
  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore;

  @Override
  public CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new MySQLCdcProcessor<>(createMySqlBinaryLogClient(), createDatabaseOffsetKafkaStore(createMySqlBinaryLogClient()), debeziumBinlogOffsetKafkaStore);
  }

  @Autowired
  private WriteRowsEventDataParser eventDataParser;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Override
  public void onEventSent(PublishedEvent publishedEvent) {
    createDatabaseOffsetKafkaStore(createMySqlBinaryLogClient()).save(publishedEvent.getBinlogFileOffset());
  }

  private MySqlBinaryLogClient<PublishedEvent> createMySqlBinaryLogClient() {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
    return new MySqlBinaryLogClient<>(eventDataParser,
            eventuateConfigurationProperties.getDbUserName(),
            eventuateConfigurationProperties.getDbPassword(),
            jdbcUrl.getHost(),
            jdbcUrl.getPort(),
            eventuateConfigurationProperties.getBinlogClientId(),
            sourceTableNameSupplier.getSourceTableName(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection());
  }

  public DatabaseOffsetKafkaStore createDatabaseOffsetKafkaStore(MySqlBinaryLogClient mySqlBinaryLogClient) {
    return new DatabaseOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(),
            mySqlBinaryLogClient.getName(),
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties,
            EventuateKafkaConsumerConfigurationProperties.empty());
  }
}
