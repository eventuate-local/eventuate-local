package io.eventuate.local.polling;

import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class PollingCdcDataPublisher<EVENT> extends CdcDataPublisher<EVENT> {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public PollingCdcDataPublisher(DataProducerFactory dataProducerFactory, PublishingStrategy<EVENT> publishingStrategy) {
    super(dataProducerFactory, publishingStrategy);
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

        if (statusService != null) {
          statusService.addPublishedEvent(json);
        }

        publishingStrategy.getCreateTime(event).ifPresent(time -> histogramEventAge.ifPresent(x -> x.set(System.currentTimeMillis() - time)));
        meterEventsPublished.ifPresent(Counter::increment);

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
