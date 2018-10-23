package io.eventuate.local.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
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
  private CdcMonitoringDao cdcMonitoringDao;
  private CdcMonitoringDataPublisher cdcMonitoringDataPublisher = new CdcMonitoringDataPublisher();

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
    this.cdcMonitoringDao = new CdcMonitoringDao(dataSource, new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA));
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

  protected void leaderStart() {
    if (meterRegistry != null) {
      cdcMonitoringDataPublisher.start();
    }
  }

  protected void leaderStop() {
    if (!running.compareAndSet(true, false)) {
      return;
    }

    if (meterRegistry != null) {
      cdcMonitoringDataPublisher.stop();
    }

    try {
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
      return;
    }
  }

  protected void onEventReceived() {
    if (meterRegistry != null) {
      Optional<Long> lastUpdate = cdcMonitoringDao.selectLastTimeUpdate(binlogClientUniqueId);

      lastUpdate
              .map(lu -> System.currentTimeMillis() - lu)
              .ifPresent(lag -> meterRegistry.gauge("eventuate.replication.lag", lag));
    }
  }

  private class CdcMonitoringDataPublisher {
    private volatile boolean run = false;
    private CountDownLatch countDownLatch;

    public void start() {
      countDownLatch = new CountDownLatch(1);
      run = true;
      publish();
    }

    public void stop() {
      run = false;
      try {
        countDownLatch.await();
      } catch (InterruptedException e) {
        logger.warn(e.getMessage(), e);
      }
    }

    private void publish() {
      new Thread(() -> {
        while (run) {

          cdcMonitoringDao.update(binlogClientUniqueId);

          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
          }
        }

        countDownLatch.countDown();
      }).start();
    }
  }
}
