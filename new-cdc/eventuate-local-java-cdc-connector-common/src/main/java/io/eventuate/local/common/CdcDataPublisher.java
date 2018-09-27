package io.eventuate.local.common;

import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.java.common.broker.DataProducer;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CdcDataPublisher<EVENT extends BinLogEvent> {
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired(required = false)
  protected MeterRegistry meterRegistry;

  protected PublishingStrategy<EVENT> publishingStrategy;
  protected DataProducerFactory dataProducerFactory;
  protected DataProducer producer;
  protected Optional<Counter> meterEventsPublished = Optional.empty();
  protected Optional<Counter> meterEventsDuplicates = Optional.empty();
  protected Optional<Counter> meterEventsRetries = Optional.empty();
  protected Optional<AtomicLong> histogramEventAge = Optional.empty();

  private PublishingFilter publishingFilter;

  public CdcDataPublisher(DataProducerFactory dataProducerFactory,
                          PublishingFilter publishingFilter,
                          PublishingStrategy<EVENT> publishingStrategy) {

    this.dataProducerFactory = dataProducerFactory;
    this.publishingStrategy = publishingStrategy;
    this.publishingFilter = publishingFilter;
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

  public void stop() {
    logger.debug("Stopping data producer");
    if (producer != null)
      producer.close();
  }

  public void handleEvent(EVENT publishedEvent) throws EventuateLocalPublishingException {

    Objects.requireNonNull(publishedEvent);

    logger.trace("Got record " + publishedEvent.toString());

    String aggregateTopic = publishingStrategy.topicFor(publishedEvent);
    String json = publishingStrategy.toJson(publishedEvent);

    Exception lastException = null;

    for (int i = 0; i < 5; i++) {
      try {
        if (publishedEvent.getBinlogFileOffset().map(o -> publishingFilter.shouldBePublished(o, aggregateTopic)).orElse(true)) {
          producer.send(
                  aggregateTopic,
                  publishingStrategy.partitionKeyFor(publishedEvent),
                  json
          ).get(10, TimeUnit.SECONDS);

          publishingStrategy.getCreateTime(publishedEvent).ifPresent(time -> histogramEventAge.ifPresent(x -> x.set(System.currentTimeMillis() - time)));
          meterEventsPublished.ifPresent(Counter::increment);
        } else {
          logger.debug("Duplicate event {}", publishedEvent);
          meterEventsDuplicates.ifPresent(Counter::increment);
        }
        return;
      } catch (Exception e) {
        logger.warn("error publishing to " + aggregateTopic, e);
        meterEventsRetries.ifPresent(Counter::increment);
        lastException = e;

        try {
          Thread.sleep((int) Math.pow(2, i) * 1000);
        } catch (InterruptedException ie) {
          throw new RuntimeException(ie);
        }
      }
    }
    throw new EventuateLocalPublishingException("error publishing to " + aggregateTopic, lastException);
  }
}
