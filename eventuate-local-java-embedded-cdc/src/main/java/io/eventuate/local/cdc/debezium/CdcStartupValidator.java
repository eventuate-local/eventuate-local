package io.eventuate.local.cdc.debezium;

import io.debezium.config.Configuration;
import io.debezium.connector.mysql.MySqlConnectorConfig;
import io.debezium.connector.mysql.MySqlJdbcContext;
import io.debezium.jdbc.JdbcConnection;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CdcStartupValidator {

  private JdbcUrl jdbcUrl;
  private String dbUser;
  private String dbPassword;
  private long mySqlValidationTimeoutMillis;
  private int mySqlValidationMaxAttempts;
  private long kafkaValidationTimeoutMillis;
  private int kafkaValidationMaxAttempts;

  private String bootstrapServers;

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public CdcStartupValidator(JdbcUrl jdbcUrl, String dbUser, String dbPassword, String bootstrapServers) {
    this.jdbcUrl = jdbcUrl;
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;
    this.bootstrapServers = bootstrapServers;
  }

  public void validateEnvironment() {
    validateDatasourceConnection();
    validateKafkaConnection();
  }

  private void validateDatasourceConnection() {
    logger.info("About to validate DataSource connection");
    Map<String, String> connectorConfig = new HashMap<>();
    connectorConfig.put(MySqlConnectorConfig.HOSTNAME.name(), jdbcUrl.getHost());
    connectorConfig.put(MySqlConnectorConfig.PORT.name(), String.valueOf(jdbcUrl.getPort()));
    connectorConfig.put(MySqlConnectorConfig.USER.name(), dbUser);
    connectorConfig.put(MySqlConnectorConfig.PASSWORD.name(), dbPassword);

    Configuration config = Configuration.from(connectorConfig);
    try (MySqlJdbcContext jdbcContext = new MySqlJdbcContext(config)) {
      jdbcContext.start();
      JdbcConnection mysql = jdbcContext.jdbc();
      int i = mySqlValidationMaxAttempts;
      SQLException lastException = null;
      while (i > 0) {
        try {
          mysql.execute("SELECT version()");
          logger.info("Successfully tested connection for {}:{} with user '{}'", jdbcContext.hostname(), jdbcContext.port(), mysql.username());
          return;
        } catch (SQLException e) {
          lastException = e;
          logger.info("Failed testing connection for {}:{} with user '{}'", jdbcContext.hostname(), jdbcContext.port(), mysql.username());
          i--;
          try {
            Thread.sleep(mySqlValidationTimeoutMillis);
          } catch (InterruptedException ie) {
            throw new RuntimeException("MySql validation had been interrupted!", ie);
          }
        }
      }
      jdbcContext.shutdown();
      throw new RuntimeException(lastException);
    }
  }

  private void validateKafkaConnection() {
    KafkaConsumer<String, String>  consumer = getTestConsumer();

    int i = kafkaValidationMaxAttempts;
    KafkaException lastException = null;
    while (i > 0) {
      try {
        consumer.listTopics();
        logger.info("Successfully tested Kafka connection");
        return;
      } catch (KafkaException e) {
        logger.info("Failed to connection to Kafka");
        lastException = e;
        i--;
        try {
          Thread.sleep(kafkaValidationTimeoutMillis);
        } catch (InterruptedException ie) {
          throw new RuntimeException("Kafka validation had been interrupted!", ie);
        }
      }
    }
    throw lastException;
  }

  private KafkaConsumer<String, String> getTestConsumer() {
    Properties consumerProperties = new Properties();
    consumerProperties.put("bootstrap.servers", bootstrapServers);
    consumerProperties.put("group.id", "stratup-test-subscriber");
    consumerProperties.put("request.timeout.ms", String.valueOf(kafkaValidationTimeoutMillis + 1));
    consumerProperties.put("session.timeout.ms", String.valueOf(kafkaValidationTimeoutMillis));
    consumerProperties.put("heartbeat.interval.ms", String.valueOf(kafkaValidationTimeoutMillis - 1));
    consumerProperties.put("fetch.max.wait.ms", String.valueOf(kafkaValidationTimeoutMillis));
    consumerProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    consumerProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

    return new KafkaConsumer<>(consumerProperties);
  }


  public void setMySqlValidationTimeoutMillis(long mySqlValidationTimeoutMillis) {
    this.mySqlValidationTimeoutMillis = mySqlValidationTimeoutMillis;
  }

  public void setMySqlValidationMaxAttempts(int mySqlValidationMaxAttempts) {
    this.mySqlValidationMaxAttempts = mySqlValidationMaxAttempts;
  }

  public void setKafkaValidationTimeoutMillis(long kafkaValidationTimeoutMillis) {
    this.kafkaValidationTimeoutMillis = kafkaValidationTimeoutMillis;
  }

  public void setKafkaValidationMaxAttempts(int kafkaValidationMaxAttempts) {
    this.kafkaValidationMaxAttempts = kafkaValidationMaxAttempts;
  }
}
