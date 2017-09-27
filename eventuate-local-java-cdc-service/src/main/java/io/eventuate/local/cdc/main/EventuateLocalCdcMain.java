package io.eventuate.local.cdc.main;

import io.eventuate.local.cdc.debezium.AbstractEventTableChangesToAggregateTopicRelay;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EventuateLocalCdcMain {

  @Bean
  public HealthIndicator relayHealthIndicator(AbstractEventTableChangesToAggregateTopicRelay relay) {
    return new RelayHealthIndicator(relay);
  }

  public static void main(String[] args) {
    SpringApplication.run(EventuateLocalCdcMain.class, args);
  }
}
