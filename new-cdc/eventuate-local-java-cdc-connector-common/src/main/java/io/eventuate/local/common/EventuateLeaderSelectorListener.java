package io.eventuate.local.common;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventuateLeaderSelectorListener implements LeaderSelectorListener {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private EventTableChangesToAggregateTopicTranslator eventTableChangesToAggregateTopicTranslator;

  public EventuateLeaderSelectorListener(EventTableChangesToAggregateTopicTranslator eventTableChangesToAggregateTopicTranslator) {
    this.eventTableChangesToAggregateTopicTranslator = eventTableChangesToAggregateTopicTranslator;
  }

  @Override
  public void takeLeadership(CuratorFramework client) throws Exception {
    takeLeadership();
  }

  private void takeLeadership() throws InterruptedException {
    logger.info("Taking leadership");
    try {
      eventTableChangesToAggregateTopicTranslator.startCapturingChanges();
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
      eventTableChangesToAggregateTopicTranslator.stopCapturingChanges();
    } catch (InterruptedException e) {
      logger.error("While handling SUSPEND", e);
    }
  }
}
