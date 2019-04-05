package io.eventuate.coordination.leadership.zookeeper;

import io.eventuate.coordination.leadership.EventuateLeaderSelector;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.CancelLeadershipException;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ZkLeaderSelector implements EventuateLeaderSelector {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private CuratorFramework curatorFramework;
  private String lockId;
  private String leaderId;
  private Runnable leaderSelectedCallback;
  private Runnable leaderRemovedCallback;
  private LeaderSelector leaderSelector;

  public ZkLeaderSelector(CuratorFramework curatorFramework,
                          String lockId,
                          Runnable leaderSelectedCallback,
                          Runnable leaderRemovedCallback) {

    this(curatorFramework, lockId, UUID.randomUUID().toString(), leaderSelectedCallback, leaderRemovedCallback);
  }

  public ZkLeaderSelector(CuratorFramework curatorFramework,
                          String lockId,
                          String leaderId,
                          Runnable leaderSelectedCallback,
                          Runnable leaderRemovedCallback) {
    this.curatorFramework = curatorFramework;
    this.lockId = lockId;
    this.leaderId = leaderId;
    this.leaderSelectedCallback = leaderSelectedCallback;
    this.leaderRemovedCallback = leaderRemovedCallback;
  }

  @Override
  public void start() {
    leaderSelector = new LeaderSelector(curatorFramework, lockId, new LeaderSelectorListener() {
      @Override
      public void takeLeadership(CuratorFramework client) {
        try {
          logger.info("Calling leaderSelectedCallback, leaderId : {}", leaderId);
          leaderSelectedCallback.run();
          logger.info("Called leaderSelectedCallback, leaderId : {}", leaderId);
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
          logger.info("Calling leaderRemovedCallback, leaderId : {}", leaderId);
          leaderRemovedCallback.run();
          logger.info("Called leaderRemovedCallback, leaderId : {}", leaderId);
          return;
        }
        while (true) {
          try {
            Thread.sleep(Long.MAX_VALUE);
          } catch (InterruptedException e) {
            break;
          }
        }
        try {
          logger.info("Calling leaderRemovedCallback, leaderId : {}", leaderId);
          leaderRemovedCallback.run();
          logger.info("Called leaderRemovedCallback, leaderId : {}", leaderId);
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        }
      }

      @Override
      public void stateChanged(CuratorFramework client, ConnectionState newState) {
        logger.info("StateChanged, state : {}, leaderId : {}", newState, leaderId);
        if (newState == ConnectionState.SUSPENDED || newState == ConnectionState.LOST) {
          throw new CancelLeadershipException();
        }
      }
    });

    leaderSelector.autoRequeue();

    leaderSelector.start();
  }

  @Override
  public void stop() {
    logger.info("Closing leader, leaderId : {}", leaderId);
    leaderSelector.close();
    logger.info("Closed leader, leaderId : {}", leaderId);
  }
}
