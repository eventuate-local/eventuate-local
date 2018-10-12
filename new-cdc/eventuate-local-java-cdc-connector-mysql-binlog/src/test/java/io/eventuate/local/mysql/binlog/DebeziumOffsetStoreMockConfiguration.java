package io.eventuate.local.mysql.binlog;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebeziumOffsetStoreMockConfiguration {

  @Bean
  public DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore() {
    return new DebeziumOffsetStoreMock();
  }
}
