package io.eventuate.local.common;

import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.java.common.broker.DataProducer;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CdcDataPublisher<EVENT extends BinLogEvent> {
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  protected MeterRegistry meterRegistry;
  protected PublishingStrategy<EVENT> publishingStrategy;
  protected DataProducerFactory dataProducerFactory;
  protected DataProducer producer;
  protected Counter meterEventsPublished;
  protected Counter meterEventsDuplicates;
  protected Counter meterEventsRetries;
  protected DistributionSummary distributionSummaryEventAge;

  private PublishingFilter publishingFilter;
  private volatile boolean lastMessagePublishingFailed;

  public CdcDataPublisher(DataProducerFactory dataProducerFactory,
                          PublishingFilter publishingFilter,
                          PublishingStrategy<EVENT> publishingStrategy,
                          MeterRegistry meterRegistry) {

    this.dataProducerFactory = dataProducerFactory;
    this.publishingStrategy = publishingStrategy;
    this.publishingFilter = publishingFilter;
    this.meterRegistry = meterRegistry;

    initMetrics();
  }

  public boolean isLastMessagePublishingFailed() {
    return lastMessagePublishingFailed;
  }

  private void initMetrics() {
    if (meterRegistry != null) {
      distributionSummaryEventAge = meterRegistry.summary("eventuate.cdc.event.age");
      meterEventsPublished = meterRegistry.counter("eventuate.cdc.events.published");
      meterEventsDuplicates = meterRegistry.counter("eventuate.cdc.events.duplicates");
      meterEventsRetries = meterRegistry.counter("eventuate.cdc.events.retries");
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

          lastMessagePublishingFailed = false;

          publishingStrategy.getCreateTime(publishedEvent).ifPresent(time -> distributionSummaryEventAge.record(System.currentTimeMillis() - time));
          meterEventsPublished.increment();
        } else {
          logger.debug("Duplicate event {}", publishedEvent);
          meterEventsDuplicates.increment();
        }
        return;
      } catch (Exception e) {

        lastMessagePublishingFailed = true;
        logger.warn("error publishing to " + aggregateTopic, e);
        meterEventsRetries.increment();
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
