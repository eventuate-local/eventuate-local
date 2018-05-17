package io.eventuate.local.common;

import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.java.common.broker.DataProducer;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.common.status.StatusService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public abstract class CdcDataPublisher<EVENT> {

  protected PublishingStrategy<EVENT> publishingStrategy;
  protected DataProducerFactory dataProducerFactory;
  protected DataProducer producer;
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired(required = false)
  protected MeterRegistry meterRegistry;

  @Autowired(required = false)
  protected StatusService statusService;

  protected Optional<Counter> meterEventsPublished = Optional.empty();
  protected Optional<Counter> meterEventsDuplicates = Optional.empty();
  protected Optional<Counter> meterEventsRetries = Optional.empty();
  protected Optional<AtomicLong> histogramEventAge = Optional.empty();

  public CdcDataPublisher(DataProducerFactory dataProducerFactory, PublishingStrategy<EVENT> publishingStrategy) {
    this.dataProducerFactory = dataProducerFactory;
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
    logger.debug("Starting CdcDataPublisher");
    producer = dataProducerFactory.create();
    logger.debug("Starting CdcDataPublisher");
  }

  public abstract void handleEvent(EVENT publishedEvent) throws EventuateLocalPublishingException;

  public void stop() {
    logger.debug("Stopping data producer");
    if (producer != null)
      producer.close();
  }

}
