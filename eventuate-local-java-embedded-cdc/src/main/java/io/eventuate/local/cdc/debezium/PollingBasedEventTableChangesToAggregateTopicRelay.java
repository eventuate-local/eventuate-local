package io.eventuate.local.cdc.debezium;


import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Monitors changes made to EVENTS table and publishes them to aggregate topics
 */
public class PollingBasedEventTableChangesToAggregateTopicRelay extends EventTableChangesToAggregateTopicRelay {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private EventPollingDao eventPollingDao;
  private int pollingIntervalInMilliseconds;
  private final AtomicBoolean watcherRunning = new AtomicBoolean();
  private volatile CompletableFuture<Void> watcherFuture = new CompletableFuture<>();

  public PollingBasedEventTableChangesToAggregateTopicRelay(
          EventPollingDao eventPollingDao,
          int pollingIntervalInMilliseconds,
          String kafkaBootstrapServers,
          CuratorFramework client,
          CdcStartupValidator cdcStartupValidator,
          TakeLeadershipAttemptTracker takeLeadershipAttemptTracker, String leadershipLockPath) {

    super(kafkaBootstrapServers, client, cdcStartupValidator, takeLeadershipAttemptTracker, leadershipLockPath);
    this.eventPollingDao = eventPollingDao;
    this.pollingIntervalInMilliseconds = pollingIntervalInMilliseconds;
  }

  public CompletableFuture<Object> startCapturingChanges() throws InterruptedException {
    logger.debug("Starting to capture changes");
    watcherRunning.set(true);

    cdcStartupValidator.validateEnvironment();
    producer = new EventuateKafkaProducer(kafkaBootstrapServers);

    CompletableFuture<Object> completableFuture = new CompletableFuture<>();

    new Thread() {
      @Override
      public void run() {

        while (watcherRunning.get()) {
          try {

            List<EventToPublish> eventToPublishes = eventPollingDao.findEventsToPublish();

            if (!eventToPublishes.isEmpty())
              logger.debug("Found {} events to publish", eventToPublishes.size());

            eventToPublishes.forEach(eventToPublish -> handleEvent(eventToPublish));

            if (!eventToPublishes.isEmpty()) {

              logger.debug("Marking {} events as published", eventToPublishes.size());

              eventPollingDao.markEventsAsPublished(eventToPublishes
                      .stream()
                      .map(EventToPublish::getEventId)
                      .collect(Collectors.toList()));
            }

            completableFuture.complete(null);

            if (eventToPublishes.isEmpty())
              try {
                logger.debug("No events. Sleeping for {} msecs", pollingIntervalInMilliseconds);
                Thread.sleep(pollingIntervalInMilliseconds);
              } catch (Exception e) {
                logger.error("error while sleeping", e);
              }
          } catch (Exception e) {
            logger.error("Exception in polling loop", e);
            completableFuture.completeExceptionally(new RuntimeException("Polling exception" + e.getMessage(), e));
          }
        }
        watcherFuture.complete(null);
        watcherFuture = new CompletableFuture<>();
      }
    }.start();

    return completableFuture;
  }

  @Override
  public void stopCapturingChanges() throws InterruptedException {

    logger.debug("Stopping to capture changes");

    if (!watcherRunning.get()) {
      return;
    }

    watcherRunning.set(false);

    if (producer != null)
      producer.close();

    try {
      watcherFuture.get(60, TimeUnit.SECONDS);
    } catch (ExecutionException | TimeoutException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

}
