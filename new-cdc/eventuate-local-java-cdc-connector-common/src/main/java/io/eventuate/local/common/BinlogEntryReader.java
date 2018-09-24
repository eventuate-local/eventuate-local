package io.eventuate.local.common;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BinlogEntryReader<HANDLER extends BinlogEntryHandler> {
  protected CuratorFramework curatorFramework;
  protected String leadershipLockPath;
  protected List<HANDLER> binlogEntryHandlers = new CopyOnWriteArrayList<>();
  protected AtomicBoolean running = new AtomicBoolean(false);
  private LeaderSelector leaderSelector;

  public BinlogEntryReader(CuratorFramework curatorFramework, String leadershipLockPath) {
    this.curatorFramework = curatorFramework;
    this.leadershipLockPath = leadershipLockPath;
  }

  public void start() {
    leaderSelector = new LeaderSelector(curatorFramework, leadershipLockPath,
            new EventuateLeaderSelectorListener(this::leaderStart, this::leaderStop));

    leaderSelector.start();
  }

  public void stop() {
    leaderSelector.close();
    leaderStop();
    binlogEntryHandlers.clear();
  }

  protected abstract void leaderStart();
  protected abstract void leaderStop();
}
