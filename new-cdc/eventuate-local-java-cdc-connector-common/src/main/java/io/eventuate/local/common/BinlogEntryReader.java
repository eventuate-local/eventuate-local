package io.eventuate.local.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BinlogEntryReader {
  protected CuratorFramework curatorFramework;
  protected String leadershipLockPath;
  protected List<BinlogEntryHandler> binlogEntryHandlers = new CopyOnWriteArrayList<>();
  protected AtomicBoolean running = new AtomicBoolean(false);
  private LeaderSelector leaderSelector;

  public BinlogEntryReader(CuratorFramework curatorFramework, String leadershipLockPath) {
    this.curatorFramework = curatorFramework;
    this.leadershipLockPath = leadershipLockPath;
  }

  public <EVENT extends BinLogEvent> void addBinlogEntryHandler(EventuateSchema eventuateSchema,
                                                                SourceTableNameSupplier sourceTableNameSupplier,
                                                                BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                                                                CdcDataPublisher<EVENT> dataPublisher) {

    BinlogEntryHandler binlogEntryHandler =
            new BinlogEntryHandler<>(eventuateSchema, sourceTableNameSupplier, binlogEntryToEventConverter, dataPublisher);

    binlogEntryHandlers.add(binlogEntryHandler);
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
