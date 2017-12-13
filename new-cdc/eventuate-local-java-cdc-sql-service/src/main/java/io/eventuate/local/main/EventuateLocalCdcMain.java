package io.eventuate.local.main;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.jmx.JmxMeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EventuateLocalCdcMain {

  @Bean
  public MeterRegistry meterRegistry() {
    return new JmxMeterRegistry();
  }

  public static void main(String[] args) {
    SpringApplication.run(EventuateLocalCdcMain.class, args);
  }
}
