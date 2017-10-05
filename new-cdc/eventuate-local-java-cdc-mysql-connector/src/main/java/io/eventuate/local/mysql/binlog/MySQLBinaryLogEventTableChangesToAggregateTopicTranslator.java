package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.mysql.binlog.exception.EventuateLocalPublishingException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class MySQLBinaryLogEventTableChangesToAggregateTopicTranslator<M extends BinLogEvent> {

  private final LeaderSelector leaderSelector;
  private MySQLCdcKafkaPublisher<M> mySQLCdcKafkaPublisher;
  private MySQLCdcProcessor<M> mySQLCdcProcessor;

  private Logger logger = LoggerFactory.getLogger(getClass());

  public MySQLBinaryLogEventTableChangesToAggregateTopicTranslator(MySQLCdcKafkaPublisher<M> mySQLCdcKafkaPublisher, MySQLCdcProcessor<M> mySQLCdcProcessor, CuratorFramework client) {
    this.mySQLCdcKafkaPublisher = mySQLCdcKafkaPublisher;
    this.mySQLCdcProcessor = mySQLCdcProcessor;
    this.leaderSelector = new LeaderSelector(client, "/eventuatelocal/cdc/leader", new LeaderSelectorListener() {

      @Override
      public void takeLeadership(CuratorFramework client) throws Exception {
        takeLeadership();
      }

      private void takeLeadership() throws InterruptedException {
        logger.info("Taking leadership");
        try {
          startCapturingChanges();
        } catch (Throwable t) {
          logger.error("In takeLeadership", t);
          throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
        } finally {
          logger.debug("TakeLeadership returning");
        }
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

  public void startCapturingChanges() throws InterruptedException {
    logger.debug("Starting to capture changes");

    mySQLCdcKafkaPublisher.start();
    try {
      mySQLCdcProcessor.start(publishedEvent -> {
        try {
          mySQLCdcKafkaPublisher.handleEvent(publishedEvent);
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

    mySQLCdcKafkaPublisher.stop();
    mySQLCdcProcessor.stop();
  }
}
