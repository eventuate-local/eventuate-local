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
  protected CdcDataPublisher cdcDataPublisher;
  protected MeterRegistry meterRegistry;
  protected CuratorFramework curatorFramework;
  protected String leadershipLockPath;
  protected List<BinlogEntryHandler> binlogEntryHandlers = new CopyOnWriteArrayList<>();
  protected AtomicBoolean running = new AtomicBoolean(false);
  protected CountDownLatch stopCountDownLatch;
  protected String dataSourceUrl;
  protected DataSource dataSource;
  protected long binlogClientUniqueId;
  protected CdcMonitoringDao cdcMonitoringDao;
  protected CommonCdcMetrics commonCdcMetrics;

  private volatile boolean leader;
  private volatile long lastEventTime = System.currentTimeMillis();
  private LeaderSelector leaderSelector;

  public BinlogEntryReader(CdcDataPublisher cdcDataPublisher,
                           MeterRegistry meterRegistry,
                           CuratorFramework curatorFramework,
                           String leadershipLockPath,
                           String dataSourceUrl,
                           DataSource dataSource,
                           long binlogClientUniqueId,
                           int monitoringRetryIntervalInMilliseconds,
                           int monitoringRetryAttempts) {

    this.cdcDataPublisher = cdcDataPublisher;
    this.meterRegistry = meterRegistry;
    this.curatorFramework = curatorFramework;
    this.leadershipLockPath = leadershipLockPath;
    this.dataSourceUrl = dataSourceUrl;
    this.dataSource = dataSource;
    this.binlogClientUniqueId = binlogClientUniqueId;


    cdcMonitoringDao = new CdcMonitoringDao(dataSource,
            new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA),
            monitoringRetryIntervalInMilliseconds,
            monitoringRetryAttempts);

    commonCdcMetrics = new CommonCdcMetrics(meterRegistry, binlogClientUniqueId);
  }

  public CdcDataPublisher getCdcDataPublisher() {
    return cdcDataPublisher;
  }

  public void setCdcDataPublisher(CdcDataPublisher cdcDataPublisher) {
    this.cdcDataPublisher = cdcDataPublisher;
  }

  public abstract CdcProcessingStatusService getCdcProcessingStatusService();

  public long getBinlogClientUniqueId() {
    return binlogClientUniqueId;
  }

  public boolean isLeader() {
    return leader;
  }

  public long getLastEventTime() {
    return lastEventTime;
  }

  public <EVENT extends BinLogEvent> void addBinlogEntryHandler(EventuateSchema eventuateSchema,
                                                                String sourceTableName,
                                                                BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                                                                PublishingStrategy<EVENT> publishingStrategy) {
    if (eventuateSchema.isEmpty()) {
      throw new IllegalArgumentException("The eventuate schema cannot be empty for the cdc processor.");
    }

    SchemaAndTable schemaAndTable = new SchemaAndTable(eventuateSchema.getEventuateDatabaseSchema(), sourceTableName);

    BinlogEntryHandler binlogEntryHandler =
            new BinlogEntryHandler<>(schemaAndTable, binlogEntryToEventConverter, publishingStrategy);

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
    commonCdcMetrics.setLeader(true);
    leader = true;
  }

  protected void leaderStop() {
    if (!running.compareAndSet(true, false)) {
      return;
    }

    try {
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
    }

    stopMetrics();
  }

  protected void stopMetrics() {
    commonCdcMetrics.setLeader(false);
    leader = false;
  }

  protected void onEventReceived() {
    commonCdcMetrics.onMessageProcessed();
    lastEventTime = System.currentTimeMillis();
  }
}
