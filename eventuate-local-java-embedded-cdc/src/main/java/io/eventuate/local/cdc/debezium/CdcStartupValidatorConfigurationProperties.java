package io.eventuate.local.cdc.debezium;

import org.springframework.beans.factory.annotation.Value;

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

  public int getMySqlValidationMaxAttempts() {
    return mySqlValidationMaxAttempts;
  }

  public long getKafkaValidationTimeoutMillis() {
    return kafkaValidationTimeoutMillis;
  }

  public int getKafkaValidationMaxAttempts() {
    return kafkaValidationMaxAttempts;
  }
}
