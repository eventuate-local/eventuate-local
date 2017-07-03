package io.eventuate.local.mysql.binlog.main;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EventuateLocalCdcMysqlBinlogMain {

  @Bean
  public ServletRegistrationBean codahaleServlet(MetricRegistry metricRegistry) {
    return new ServletRegistrationBean(new MetricsServlet(metricRegistry), "/codahalemetrics");
  }

  public static void main(String[] args) {
    SpringApplication.run(EventuateLocalCdcMysqlBinlogMain.class, args);
  }
}
