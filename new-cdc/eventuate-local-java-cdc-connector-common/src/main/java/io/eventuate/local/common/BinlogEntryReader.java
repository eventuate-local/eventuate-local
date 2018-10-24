package io.eventuate.local.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BinlogEntryReader {
  protected Logger logger = LoggerFactory.getLogger(getClass());
  protected MeterRegistry meterRegistry;
  protected CuratorFramework curatorFramework;
  protected String leadershipLockPath;
  protected List<BinlogEntryHandler> binlogEntryHandlers = new CopyOnWriteArrayList<>();
  protected AtomicBoolean running = new AtomicBoolean(false);
  protected CountDownLatch stopCountDownLatch;
  protected String dataSourceUrl;
  protected DataSource dataSource;
  protected long binlogClientUniqueId;
  private LeaderSelector leaderSelector;

  public BinlogEntryReader(MeterRegistry meterRegistry,
                           CuratorFramework curatorFramework,
                           String leadershipLockPath,
                           String dataSourceUrl,
                           DataSource dataSource,
                           long binlogClientUniqueId) {

    this.meterRegistry = meterRegistry;
    this.curatorFramework = curatorFramework;
    this.leadershipLockPath = leadershipLockPath;
    this.dataSourceUrl = dataSourceUrl;
    this.dataSource = dataSource;
    this.binlogClientUniqueId = binlogClientUniqueId;
  }

  public <EVENT extends BinLogEvent> void addBinlogEntryHandler(EventuateSchema eventuateSchema,
                                                                String sourceTableName,
                                                                BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                                                                CdcDataPublisher<EVENT> dataPublisher) {
    if (eventuateSchema.isEmpty()) {
      throw new IllegalArgumentException("The eventuate schema cannot be empty for the cdc processor.");
    }

    SchemaAndTable schemaAndTable = new SchemaAndTable(eventuateSchema.getEventuateDatabaseSchema(), sourceTableName);

    BinlogEntryHandler binlogEntryHandler =
            new BinlogEntryHandler<>(schemaAndTable, binlogEntryToEventConverter, dataPublisher);

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

  protected void leaderStop() {
    if (!running.compareAndSet(true, false)) {
      return;
    }

    try {
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
    }
  }
}
