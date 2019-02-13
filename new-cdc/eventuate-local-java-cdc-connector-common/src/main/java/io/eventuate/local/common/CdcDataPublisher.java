package io.eventuate.local.common;

import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.java.common.broker.DataProducer;
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
  protected DataProducer producer;
  protected Counter meterEventsPublished;
  protected Counter meterEventsDuplicates;
  protected Counter meterEventsRetries;
  protected DistributionSummary distributionSummaryEventAge;

  private volatile boolean lastMessagePublishingFailed;

  public CdcDataPublisher(DataProducer dataProducer,
                          MeterRegistry meterRegistry) {

    this.producer = dataProducer;
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

  public void handleEvent(EVENT publishedEvent, PublishingStrategy<EVENT> publishingStrategy) throws EventuateLocalPublishingException {

    Objects.requireNonNull(publishedEvent);

    logger.trace("Got record " + publishedEvent.toString());

    String aggregateTopic = publishingStrategy.topicFor(publishedEvent);
    String json = publishingStrategy.toJson(publishedEvent);

    Exception lastException = null;

    for (int i = 0; i < 5; i++) {
      try {
        producer.send(
                aggregateTopic,
                publishingStrategy.partitionKeyFor(publishedEvent),
                json
        ).get(10, TimeUnit.SECONDS);

        lastMessagePublishingFailed = false;

        publishingStrategy.getCreateTime(publishedEvent).ifPresent(time -> distributionSummaryEventAge.record(System.currentTimeMillis() - time));
        meterEventsPublished.increment();
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
