package io.eventuate.local.publisher;

import org.springframework.boot.SpringApplication;

public class PublisherMain {

  public static void main(String[] args) {
    SpringApplication.run(EventuateLocalPublisherConfiguration.class);
  }
}
