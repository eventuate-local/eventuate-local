package io.eventuate.local.common;

import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class EventTableChangesToAggregateTopicTranslator<EVENT> {

  private final LeaderSelector leaderSelector;
  private CdcKafkaPublisher<EVENT> cdcKafkaPublisher;
  private CdcProcessor<EVENT> cdcProcessor;

  private Logger logger = LoggerFactory.getLogger(getClass());

  public EventTableChangesToAggregateTopicTranslator(CdcKafkaPublisher<EVENT> cdcKafkaPublisher,
          CdcProcessor<EVENT> cdcProcessor,
          CuratorFramework client,
          String leadershipLockPath) {

    this.cdcKafkaPublisher = cdcKafkaPublisher;
    this.cdcProcessor = cdcProcessor;
    this.leaderSelector = new LeaderSelector(client, leadershipLockPath,
        new EventuateLeaderSelectorListener(this));
  }

  @PostConstruct
  public void start() {
    logger.info("CDC initialized. Ready to become leader");
    leaderSelector.start();
  }

  public void startCapturingChanges() throws InterruptedException {
    logger.debug("Starting to capture changes");

    cdcKafkaPublisher.start();
    try {
      cdcProcessor.start(publishedEvent -> {
        try {
          cdcKafkaPublisher.handleEvent(publishedEvent);
        } catch (EventuateLocalPublishingException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (Exception e) {
      if (e.getCause() instanceof EventuateLocalPublishingException) {
        logger.error("Stopping capturing changes due to exception:", e);
        this.stopCapturingChanges();
      }
    }

    logger.debug("Started CDC Kafka publisher");
  }

  @PreDestroy
  public void stop() throws InterruptedException {
    //stopCapturingChanges();
    leaderSelector.close();
  }

  public void stopCapturingChanges() throws InterruptedException {
    logger.debug("Stopping to capture changes");

    cdcKafkaPublisher.stop();
    cdcProcessor.stop();
  }
}
