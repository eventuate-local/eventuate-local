package io.eventuate.local.db.log.common;

import com.google.common.collect.ImmutableList;
import io.eventuate.local.common.AbstractCdcMetrics;
import io.eventuate.local.common.CdcMonitoringDao;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DbLogMetrics extends AbstractCdcMetrics {
  private Timer eventPublisherTimer;

  private CdcMonitoringDao cdcMonitoringDao;
  private long replicationLagMeasuringIntervalInMilliseconds;

  private DistributionSummary lag;
  private AtomicInteger connected = new AtomicInteger(0);

  private volatile long lastTimeEventReceived = -1;

  public DbLogMetrics(MeterRegistry meterRegistry,
                      CdcMonitoringDao cdcMonitoringDao,
                      String readerName,
                      long replicationLagMeasuringIntervalInMilliseconds) {

    super(meterRegistry, readerName);

    this.cdcMonitoringDao = cdcMonitoringDao;
    this.replicationLagMeasuringIntervalInMilliseconds = replicationLagMeasuringIntervalInMilliseconds;
    tags = ImmutableList.of(Tag.of("readerName", readerName));

    initMetrics();
  }

  public void start() {
    if (meterRegistry == null) {
      return;
    }

    initLagMeasurementTimer();
  }

  public void stop() {
    if (meterRegistry == null) {
      return;
    }

    eventPublisherTimer.cancel();
  }

  public void onLagMeasurementEventReceived(long timestamp) {

    if (meterRegistry == null) {
      return;
    }

    lastTimeEventReceived = System.currentTimeMillis();

    lag.record(System.currentTimeMillis() - timestamp);
  }

  public void onBinlogEntryProcessed() {
    meterRegistry.counter("eventuate.cdc.binlog.entries.processed", tags).increment();
  }

  public void onConnected() {
    connected.set(1);
    meterRegistry.counter("eventuate.cdc.connection.attempts", tags).increment();
  }

  public void onDisconnected() {
    connected.set(0);
  }

  private void initLagMeasurementTimer() {
    eventPublisherTimer = new Timer();

    eventPublisherTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        cdcMonitoringDao.update(readerName);
      }
    }, 0, replicationLagMeasuringIntervalInMilliseconds);
  }

  private void initMetrics() {
    if (meterRegistry != null) {
      Number lagAge = new Number() {
        @Override
        public int intValue() {
          return -1;
        }

        @Override
        public long longValue() {
          if (lastTimeEventReceived == -1) {
            return -1;
          }

          return System.currentTimeMillis() - lastTimeEventReceived;
        }

        @Override
        public float floatValue() {
          return -1;
        }

        @Override
        public double doubleValue() {
          return longValue();
        }
      };

      lag = meterRegistry.summary("eventuate.cdc.replication.lag", tags);
      meterRegistry.gauge("eventuate.cdc.replication.lag.age", tags, lagAge);
      meterRegistry.gauge("eventuate.cdc.connected.to.database", tags, connected);
    }
  }
}
