package io.eventuate.local.mysql.binlog;

import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.exception.EventuateLocalPublishingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;

import java.util.concurrent.TimeUnit;

public class PollingCdcKafkaPublisher<EVENT> {

  private String kafkaBootstrapServers;
  private PublishingStrategy<EVENT> publishingStrategy;
  private EventuateKafkaProducer producer;
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private GaugeService gaugeService;

  @Autowired
  private CounterService counterService;

  public PollingCdcKafkaPublisher(String kafkaBootstrapServers, PublishingStrategy<EVENT> publishingStrategy) {
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    this.publishingStrategy = publishingStrategy;
  }

  public void start() {
    logger.debug("Starting PollingCdcKafkaPublisher");
    producer = new EventuateKafkaProducer(kafkaBootstrapServers);
    logger.debug("Starting PollingCdcKafkaPublisher");
  }

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

        if (gaugeService != null)
          publishingStrategy.getCreateTime(event).ifPresent(time ->
              gaugeService.submit("histogram.event.age", System.currentTimeMillis() - time));

        if (counterService != null)
          counterService.increment("meter.events.published");

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
