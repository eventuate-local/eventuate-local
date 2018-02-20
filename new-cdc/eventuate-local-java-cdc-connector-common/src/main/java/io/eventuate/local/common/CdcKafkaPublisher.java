package io.eventuate.local.common;

import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public abstract class CdcKafkaPublisher<EVENT> {

  private String kafkaBootstrapServers;
  protected PublishingStrategy<EVENT> publishingStrategy;
  protected EventuateKafkaProducer producer;
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired(required = false)
  protected MeterRegistry meterRegistry;

  protected Optional<Counter> meterEventsPublished = Optional.empty();
  protected Optional<Counter> meterEventsDuplicates = Optional.empty();
  protected Optional<Counter> meterEventsRetries = Optional.empty();
  protected Optional<AtomicLong> histogramEventAge = Optional.empty();

  public CdcKafkaPublisher(String kafkaBootstrapServers, PublishingStrategy<EVENT> publishingStrategy) {
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    this.publishingStrategy = publishingStrategy;
  }

  @PostConstruct
  private void initMetrics() {
    if (meterRegistry != null) {

      histogramEventAge = Optional.of(meterRegistry.gauge("histogram.event.age", new AtomicLong(0)));
      meterEventsPublished = Optional.of(meterRegistry.counter("meter.events.published"));
      meterEventsDuplicates = Optional.of(meterRegistry.counter("meter.events.duplicates"));
      meterEventsRetries = Optional.of(meterRegistry.counter("meter.events.retries"));
    }
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
