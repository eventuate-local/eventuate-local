package io.eventuate.local.main;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.jmx.JmxMeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class EventuateLocalCdcMain {

  @Bean
  public MeterRegistry meterRegistry() {
    return new JmxMeterRegistry();
  }

  public static void main(String[] args) {
    System.setProperty("dry-run-cdc-migration",
            String.valueOf(Arrays.asList(args).contains("--dry-run-cdc-migration")));

    SpringApplication.run(EventuateLocalCdcMain.class, args);
  }
}
