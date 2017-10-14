package io.eventuate.local.cdc.debezium;


import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.common.AggregateTopicMapping;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Subscribes to changes made to EVENTS table and publishes them to aggregate topics
 */
public abstract class EventTableChangesToAggregateTopicRelay {

  private Logger logger = LoggerFactory.getLogger(getClass());

  protected EventuateKafkaProducer producer;
  protected String kafkaBootstrapServers;
  protected CdcStartupValidator cdcStartupValidator;

  private final LeaderSelector leaderSelector;

  private AtomicReference<RelayStatus> status = new AtomicReference<>(RelayStatus.IDLE);

  public RelayStatus getStatus() {
    return status.get();
  }

  private final TakeLeadershipAttemptTracker takeLeadershipAttemptTracker;

  protected EventTableChangesToAggregateTopicRelay(String kafkaBootstrapServers,
                                                CuratorFramework client,
                                                CdcStartupValidator cdcStartupValidator,
                                                TakeLeadershipAttemptTracker takeLeadershipAttemptTracker, String leadershipLockPath) {

    this.kafkaBootstrapServers = kafkaBootstrapServers;

    this.cdcStartupValidator = cdcStartupValidator;
    this.takeLeadershipAttemptTracker = takeLeadershipAttemptTracker;

    leaderSelector = new LeaderSelector(client, leadershipLockPath, new LeaderSelectorListener() {

      @Override
      public void takeLeadership(CuratorFramework client) throws Exception {
        takeLeadership();
      }

      private void takeLeadership() throws InterruptedException {

        RuntimeException laste = null;
        do {
          takeLeadershipAttemptTracker.attempting();
          status.set(RelayStatus.RUNNING);
          logger.info("Taking leadership");
          try {
            CompletableFuture<Object> completion = startCapturingChanges();
            try {
              completion.get();
            } catch (InterruptedException e) {
              logger.error("Interrupted while taking leadership");
            }
            return;
          } catch (Throwable t) {
            switch (status.get()) {
              case RUNNING:
                status.set(RelayStatus.FAILED);
            }
            logger.error("In takeLeadership", t);
            laste = t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
            doResign();
          } finally {
            logger.debug("TakeLeadership returning");
          }
        } while (status.get() == RelayStatus.FAILED && takeLeadershipAttemptTracker.shouldAttempt());
        throw laste;
      }

      @Override
      public void stateChanged(CuratorFramework client, ConnectionState newState) {

        logger.debug("StateChanged: {}", newState);

        switch (newState) {
          case SUSPENDED:
            resignLeadership();
            break;

          case RECONNECTED:
            try {
              takeLeadership();
            } catch (InterruptedException e) {
              logger.error("While handling RECONNECTED", e);
            }
            break;

          case LOST:
            resignLeadership();
            break;
        }
      }

      private void resignLeadership() {
        status.set(RelayStatus.STOPPING);
        doResign();
        status.set(RelayStatus.IDLE);
      }

      private void doResign() {
        logger.info("Resigning leadership");
        try {
          stopCapturingChanges();
        } catch (InterruptedException e) {
          logger.error("While handling SUSPEND", e);
        }
      }
    });
  }

  @PostConstruct
  public void start() {
    logger.info("CDC initialized. Ready to become leader");
    leaderSelector.start();
  }

  public abstract CompletableFuture<Object> startCapturingChanges() throws InterruptedException;

  @PreDestroy
  public void stop() throws InterruptedException {
    //stopCapturingChanges();
    leaderSelector.close();
  }

  public abstract void stopCapturingChanges() throws InterruptedException;


  public static String toJson(PublishedEvent eventInfo) {
    return JSonMapper.toJson(eventInfo);
  }

  protected void handleEvent(EventToPublish eventToPublish) {

    PublishedEvent pe = new PublishedEvent(eventToPublish.getEventId(),
        eventToPublish.getEntityId(),
        eventToPublish.getEntityType(),
        eventToPublish.getEventData(),
        eventToPublish.getEventType(),
        null,
        eventToPublish.getMetadataOptional());


    String aggregateTopic = AggregateTopicMapping.aggregateTypeToTopic(pe.getEntityType());
    String json = toJson(pe);

    if (logger.isInfoEnabled())
      logger.debug("Publishing triggeringEvent={}, event={}", eventToPublish.getTriggeringEvent(), json);

    try {
      producer.send(aggregateTopic,
          eventToPublish.getEntityId(),
          json).get(10, TimeUnit.SECONDS);
    } catch (RuntimeException e) {
      logger.error("error publishing to " + aggregateTopic, e);
      throw e;
    } catch (Throwable e) {
      logger.error("error publishing to " + aggregateTopic, e);
      throw new RuntimeException(e);
    }
  }

//  public static class MyKafkaOffsetBackingStore extends KafkaOffsetBackingStore {
//
//    @Override
//    public void configure(WorkerConfig configs) {
//      Map<String, Object> updatedConfig = new HashMap<>(configs.originals());
//      updatedConfig.put("bootstrap.servers", kafkaBootstrapServers);
//      super.configure(new WorkerConfig(configs.));
//    }
//  }

}
