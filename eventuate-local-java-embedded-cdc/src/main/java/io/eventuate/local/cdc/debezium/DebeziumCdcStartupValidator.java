package io.eventuate.local.cdc.debezium;

import io.debezium.config.Configuration;
import io.debezium.connector.mysql.MySqlConnectorConfig;
import io.debezium.connector.mysql.MySqlJdbcContext;
import io.debezium.jdbc.JdbcConnection;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DebeziumCdcStartupValidator extends CdcStartupValidator {

  private JdbcUrl jdbcUrl;
  private String dbUser;
  private String dbPassword;
  private long mySqlValidationTimeoutMillis;
  private int mySqlValidationMaxAttempts;

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public DebeziumCdcStartupValidator(JdbcUrl jdbcUrl,
                                     String dbUser,
                                     String dbPassword,
                                     String bootstrapServers,
                                     EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
    super(bootstrapServers, eventuateKafkaConsumerConfigurationProperties);
    this.jdbcUrl = jdbcUrl;
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;
  }

  @Override
  public void validateEnvironment() {
    super.validateEnvironment();
    validateDatasourceConnection();
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
          logger.warn(String.format("Failed testing connection for %s:%s with user '%s'", jdbcContext.hostname(), jdbcContext.port(), mysql.username()), e);
          i--;
          try {
            Thread.sleep(mySqlValidationTimeoutMillis);
          } catch (InterruptedException ie) {
            throw new RuntimeException("MySql validation had been interrupted!", ie);
          }
        }
      }
      logger.error(String.format("Could not connect to database for %s:%s with user '%s'", jdbcContext.hostname(), jdbcContext.port(), mysql.username()), lastException);
      throw new RuntimeException(lastException);
    }
  }

  public void setMySqlValidationTimeoutMillis(long mySqlValidationTimeoutMillis) {
    this.mySqlValidationTimeoutMillis = mySqlValidationTimeoutMillis;
  }

  public void setMySqlValidationMaxAttempts(int mySqlValidationMaxAttempts) {
    this.mySqlValidationMaxAttempts = mySqlValidationMaxAttempts;
  }
}
