package io.eventuate.local.cdc.debezium;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class CdcStartupValidatorConfigurationProperties {

  @Value("${eventuatelocal.startup.validator.mysql.validation.timeout.millis:#{1000}}")
  private long mySqlValidationTimeoutMillis = 1000;

  @Value("${eventuatelocal.startup.validator.mysql.validation.max.attempts:#{100}}")
  private int mySqlValidationMaxAttempts = 100;

  @Value("${eventuatelocal.startup.validator.kafka.validation.timeout.millis:#{1000}}")
  private long kafkaValidationTimeoutMillis = 1000;

  @Value("${eventuatelocal.startup.validator.kafka.validation.max.attempts:#{100}}")
  private int kafkaValidationMaxAttempts = 100;

  public long getMySqlValidationTimeoutMillis() {
    return mySqlValidationTimeoutMillis;
  }

  public void setMySqlValidationTimeoutMillis(long mySqlValidationTimeoutMillis) {
    this.mySqlValidationTimeoutMillis = mySqlValidationTimeoutMillis;
  }

  public int getMySqlValidationMaxAttempts() {
    return mySqlValidationMaxAttempts;
  }

  public void setMySqlValidationMaxAttempts(int mySqlValidationMaxAttempts) {
    this.mySqlValidationMaxAttempts = mySqlValidationMaxAttempts;
  }

  public long getKafkaValidationTimeoutMillis() {
    return kafkaValidationTimeoutMillis;
  }

  public void setKafkaValidationTimeoutMillis(long kafkaValidationTimeoutMillis) {
    this.kafkaValidationTimeoutMillis = kafkaValidationTimeoutMillis;
  }

  public int getKafkaValidationMaxAttempts() {
    return kafkaValidationMaxAttempts;
  }

  public void setKafkaValidationMaxAttempts(int kafkaValidationMaxAttempts) {
    this.kafkaValidationMaxAttempts = kafkaValidationMaxAttempts;
  }
}
