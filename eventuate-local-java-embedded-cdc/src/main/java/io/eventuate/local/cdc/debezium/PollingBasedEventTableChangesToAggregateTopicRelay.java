package io.eventuate.local.cdc.debezium;


import com.google.common.collect.ImmutableMap;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Monitors changes made to EVENTS table and publishes them to aggregate topics
 */
public class PollingBasedEventTableChangesToAggregateTopicRelay extends EventTableChangesToAggregateTopicRelay {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private EventPollingDao eventPollingDao;
  private int requestPeriodInMilliseconds;
  private int maxEventsPerPolling;
  private boolean watcherRunning = false;

  public PollingBasedEventTableChangesToAggregateTopicRelay(
    EventPollingDao eventPollingDao,
    int requestPeriodInMilliseconds,
    String kafkaBootstrapServers,
    CuratorFramework client,
    CdcStartupValidator cdcStartupValidator,
    TakeLeadershipAttemptTracker takeLeadershipAttemptTracker) {

    super(kafkaBootstrapServers, client, cdcStartupValidator, takeLeadershipAttemptTracker);
    this.eventPollingDao = eventPollingDao;
    this.requestPeriodInMilliseconds = requestPeriodInMilliseconds;
    this.maxEventsPerPolling = maxEventsPerPolling;
  }

  public CompletableFuture<Object> startCapturingChanges() throws InterruptedException {
    logger.debug("Starting to capture changes");

    cdcStartupValidator.validateEnvironment();

    watcherRunning = true;
    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    producer = new EventuateKafkaProducer(kafkaBootstrapServers);

    new Thread() {
      @Override
      public void run() {

        while (watcherRunning) {
          try {

            List<EventToPublish> eventToPublishes = eventPollingDao.findEventsToPublish();

            eventToPublishes.forEach(eventToPublish -> handleEvent(eventToPublish));

            if (!eventToPublishes.isEmpty()) {

              eventPollingDao.markEventsAsPublished(eventToPublishes
                .stream()
                .map(EventToPublish::getEventId)
                .collect(Collectors.toList()));
            }

            completableFuture.complete(null);

            try {
              Thread.sleep(requestPeriodInMilliseconds);
            } catch (Exception e) {
              logger.error(e.getMessage(), e);
            }
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            completableFuture.completeExceptionally(new RuntimeException("Polling exception" + e.getMessage(), e));
          }
        }
      }
    }.start();

    return completableFuture;
  }

  @Override
  public void stopCapturingChanges() throws InterruptedException {

    logger.debug("Stopping to capture changes");

    if (producer != null)
      producer.close();

    watcherRunning = false;
  }

}
