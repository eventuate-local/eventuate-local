package io.eventuate.local.common;

import io.eventuate.coordination.leadership.EventuateLeaderSelector;
import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BinlogEntryReader {
  protected Logger logger = LoggerFactory.getLogger(getClass());
  protected MeterRegistry meterRegistry;
  protected String leaderLockId;
  protected LeaderSelectorFactory leaderSelectorFactory;
  protected List<BinlogEntryHandler> binlogEntryHandlers = new CopyOnWriteArrayList<>();
  protected AtomicBoolean running = new AtomicBoolean(false);
  protected CountDownLatch stopCountDownLatch;
  protected String dataSourceUrl;
  protected DataSource dataSource;
  protected String readerName;
  protected CommonCdcMetrics commonCdcMetrics;

  private volatile boolean leader;
  private volatile long lastEventTime = System.currentTimeMillis();
  private EventuateLeaderSelector leaderSelector;

  public BinlogEntryReader(MeterRegistry meterRegistry,
                           String leaderLockId,
                           LeaderSelectorFactory leaderSelectorFactory,
                           String dataSourceUrl,
                           DataSource dataSource,
                           String readerName) {

    this.meterRegistry = meterRegistry;
    this.leaderLockId = leaderLockId;
    this.leaderSelectorFactory = leaderSelectorFactory;
    this.dataSourceUrl = dataSourceUrl;
    this.dataSource = dataSource;
    this.readerName = readerName;

    commonCdcMetrics = new CommonCdcMetrics(meterRegistry, readerName);
  }

  public abstract CdcProcessingStatusService getCdcProcessingStatusService();

  public String getReaderName() {
    return readerName;
  }

  public boolean isLeader() {
    return leader;
  }

  public long getLastEventTime() {
    return lastEventTime;
  }

  public <EVENT extends BinLogEvent> BinlogEntryHandler addBinlogEntryHandler(EventuateSchema eventuateSchema,
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

    return binlogEntryHandler;
  }

  public void start() {
    leaderSelector = leaderSelectorFactory.create(leaderLockId, UUID.randomUUID().toString(), this::leaderStart, this::leaderStop);
    leaderSelector.start();
  }

  public void stop() {
    leaderSelector.stop();
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
    onActivity();
  }

  protected void onActivity() {
    lastEventTime = System.currentTimeMillis();
  }

  protected void handleProcessingFailException(Exception e) {
    logger.error(e.getMessage(), e);
    stopCountDownLatch.countDown();
    throw new RuntimeException(e);
  }
}
