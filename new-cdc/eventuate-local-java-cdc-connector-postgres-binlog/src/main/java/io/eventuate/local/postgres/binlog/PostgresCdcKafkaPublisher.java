package io.eventuate.local.postgres.binlog;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.CdcKafkaPublisher;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class PostgresCdcKafkaPublisher<EVENT extends BinLogEvent> extends CdcKafkaPublisher<EVENT> {

  private DatabaseLastSequenceNumberKafkaStore databaseLastSequenceNumberKafkaStore;
  private DuplicatePublishingDetector duplicatePublishingDetector;
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public PostgresCdcKafkaPublisher(DatabaseLastSequenceNumberKafkaStore databaseLastSequenceNumberKafkaStore, String kafkaBootstrapServers, PublishingStrategy<EVENT> publishingStrategy) {
    super(kafkaBootstrapServers, publishingStrategy);

    this.databaseLastSequenceNumberKafkaStore = databaseLastSequenceNumberKafkaStore;
    this.duplicatePublishingDetector = new DuplicatePublishingDetector(kafkaBootstrapServers);
  }

  @Override
  public void handleEvent(EVENT publishedEvent) throws EventuateLocalPublishingException {
    logger.trace("Got record " + publishedEvent.toString());

    String aggregateTopic = publishingStrategy.topicFor(publishedEvent);
    String json = publishingStrategy.toJson(publishedEvent);

    Exception lastException = null;

    for (int i = 0; i < 5; i++) {
      try {
        if (duplicatePublishingDetector.shouldBePublished(publishedEvent.getBinlogFileOffset(), aggregateTopic)) {
          producer.send(
                  aggregateTopic,
                  publishingStrategy.partitionKeyFor(publishedEvent),
                  json
          ).get(10, TimeUnit.SECONDS);

          if (gaugeService != null)
            publishingStrategy.getCreateTime(publishedEvent).ifPresent(time -> gaugeService.submit("histogram.event.age", System.currentTimeMillis() - time));

          if (counterService != null)
            counterService.increment("meter.events.published");

          databaseLastSequenceNumberKafkaStore.save(publishedEvent.getBinlogFileOffset());
        } else {
          logger.debug("Duplicate event {}", publishedEvent);
          if (counterService != null)
            counterService.increment("meter.events.duplicates");
        }
        return;
      } catch (Exception e) {
        logger.warn("error publishing to " + aggregateTopic, e);
        if (counterService != null)
          counterService.increment("meter.events.retries");
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
