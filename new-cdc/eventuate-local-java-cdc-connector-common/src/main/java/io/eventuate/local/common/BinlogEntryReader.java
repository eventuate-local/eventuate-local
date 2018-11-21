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
  protected CdcMonitoringDao cdcMonitoringDao;
  protected CommonCdcMetrics commonCdcMetrics;
  protected HealthCheck.HealthComponent healthComponent;

  private long lastEventTime = System.currentTimeMillis();
  private int maxEventIntervalToAssumeReaderHealthy;
  private HealthCheck healthCheck;
  private LeaderSelector leaderSelector;

  public BinlogEntryReader(MeterRegistry meterRegistry,
                           HealthCheck healthCheck,
                           CuratorFramework curatorFramework,
                           String leadershipLockPath,
                           String dataSourceUrl,
                           DataSource dataSource,
                           long binlogClientUniqueId,
                           int monitoringRetryIntervalInMilliseconds,
                           int monitoringRetryAttempts,
                           int maxEventIntervalToAssumeReaderHealthy) {

    this.meterRegistry = meterRegistry;
    this.healthCheck = healthCheck;
    this.curatorFramework = curatorFramework;
    this.leadershipLockPath = leadershipLockPath;
    this.dataSourceUrl = dataSourceUrl;
    this.dataSource = dataSource;
    this.binlogClientUniqueId = binlogClientUniqueId;
    this.maxEventIntervalToAssumeReaderHealthy = maxEventIntervalToAssumeReaderHealthy;


    cdcMonitoringDao = new CdcMonitoringDao(dataSource,
            new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA),
            monitoringRetryIntervalInMilliseconds,
            monitoringRetryAttempts);

    commonCdcMetrics = new CommonCdcMetrics(meterRegistry, binlogClientUniqueId);
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
    healthComponent = healthCheck.getHealthComponent(this::checkIfEventIsReceivedRecently);

    leaderSelector = new LeaderSelector(curatorFramework, leadershipLockPath,
            new EventuateLeaderSelectorListener(this::leaderStart, this::leaderStop));

    leaderSelector.start();
  }

  public void stop() {
    healthCheck.returnHealthComponent(healthComponent);

    leaderSelector.close();
    leaderStop();
    binlogEntryHandlers.clear();
  }

  protected void leaderStart() {
    commonCdcMetrics.setLeader(true);
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
    healthComponent.markAsHealthy();
  }

  protected void onEventReceived() {
    commonCdcMetrics.onMessageProcessed();
    lastEventTime = System.currentTimeMillis();
    healthComponent.markAsHealthy();
  }

  protected void checkIfEventIsReceivedRecently() {
    if (System.currentTimeMillis() - lastEventTime > maxEventIntervalToAssumeReaderHealthy) {
      healthComponent.markAsUnhealthyIfHealthy(String.format("No events received recently by reader %s", binlogClientUniqueId));
    }
  }
}
