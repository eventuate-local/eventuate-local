package io.eventuate.local.polling;

import io.eventuate.local.common.CdcKafkaPublisher;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class PollingCdcKafkaPublisher<EVENT> extends CdcKafkaPublisher<EVENT> {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public PollingCdcKafkaPublisher(String kafkaBootstrapServers, PublishingStrategy<EVENT> publishingStrategy) {
    super(kafkaBootstrapServers, publishingStrategy);
  }

  @Override
  public void handleEvent(EVENT event) throws EventuateLocalPublishingException {
    logger.trace("Got record " + event.toString());

    String aggregateTopic = publishingStrategy.topicFor(event);
    String json = publishingStrategy.toJson(event);

    Exception lastException = null;

    for (int i = 0; i < 5; i++) {
      try {
        producer.send(
                aggregateTopic,
                publishingStrategy.partitionKeyFor(event),
                json
        ).get(10, TimeUnit.SECONDS);

        publishingStrategy.getCreateTime(event).ifPresent(time -> histogramEventAge.set(System.currentTimeMillis() - time));

        if (meterEventsPublished != null) meterEventsPublished.increment();

        return;
      } catch (Exception e) {
        logger.warn("error publishing to " + aggregateTopic, e);

        if (meterEventsRetries != null) meterEventsRetries.increment();

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
