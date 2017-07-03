package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.exception.EventuateLocalPublishingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;

import java.util.concurrent.TimeUnit;

public class MySQLCdcKafkaPublisher<M extends BinLogEvent> {

  private String kafkaBootstrapServers;
  private PublishingStrategy<M> publishingStrategy;
  private DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore;
  private EventuateKafkaProducer producer;
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private DuplicatePublishingDetector duplicatePublishingDetector;

  @Autowired
  private GaugeService gaugeService;
  @Autowired
  private CounterService counterService;

  public MySQLCdcKafkaPublisher(DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore, String kafkaBootstrapServers, PublishingStrategy<M> publishingStrategy) {
    this.binlogOffsetKafkaStore = binlogOffsetKafkaStore;
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    this.publishingStrategy = publishingStrategy;
    this.duplicatePublishingDetector = new DuplicatePublishingDetector(kafkaBootstrapServers);
  }

  public void start() {
    logger.debug("Starting MySQLCdcKafkaPublisher");
    producer = new EventuateKafkaProducer(kafkaBootstrapServers);
    logger.debug("Starting MySQLCdcKafkaPublisher");
  }

  public void handleEvent(M publishedEvent) throws EventuateLocalPublishingException {
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

          binlogOffsetKafkaStore.save(publishedEvent.getBinlogFileOffset());
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


  public void stop() {
    logger.debug("Stopping kafka producer");
    if (producer != null)
      producer.close();
  }

}
