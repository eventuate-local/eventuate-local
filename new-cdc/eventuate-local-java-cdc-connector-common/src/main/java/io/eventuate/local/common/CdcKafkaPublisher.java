package io.eventuate.local.common;

import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;

public abstract class CdcKafkaPublisher<EVENT> {

  private String kafkaBootstrapServers;
  protected PublishingStrategy<EVENT> publishingStrategy;
  protected EventuateKafkaProducer producer;
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired(required = false)
  protected GaugeService gaugeService;

  @Autowired(required = false)
  protected CounterService counterService;

  public CdcKafkaPublisher(String kafkaBootstrapServers, PublishingStrategy<EVENT> publishingStrategy) {
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    this.publishingStrategy = publishingStrategy;
  }

  public void start() {
    logger.debug("Starting CdcKafkaPublisher");
    producer = new EventuateKafkaProducer(kafkaBootstrapServers);
    logger.debug("Starting CdcKafkaPublisher");
  }

  public abstract void handleEvent(EVENT publishedEvent) throws EventuateLocalPublishingException;

  public void stop() {
    logger.debug("Stopping kafka producer");
    if (producer != null)
      producer.close();
  }

}
