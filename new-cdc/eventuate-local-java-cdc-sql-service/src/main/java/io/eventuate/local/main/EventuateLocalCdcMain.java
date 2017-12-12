package io.eventuate.local.main;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

@SpringBootApplication
public class EventuateLocalCdcMain {

  @Bean
  public MeterRegistry meterRegistry() {
    PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    try {
      HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
      server.createContext("/prometheus", httpExchange -> {
        String response = prometheusRegistry.scrape();
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
      });

      new Thread(server::start).run();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return prometheusRegistry;
  }

  public static void main(String[] args) {
    SpringApplication.run(EventuateLocalCdcMain.class, args);
  }
}
