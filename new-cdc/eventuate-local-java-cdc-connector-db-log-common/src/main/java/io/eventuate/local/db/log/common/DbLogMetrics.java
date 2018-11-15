package io.eventuate.local.db.log.common;

import com.google.common.collect.ImmutableList;
import io.eventuate.local.common.AbstractCdcMetrics;
import io.eventuate.local.common.CdcMonitoringDao;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DbLogMetrics extends AbstractCdcMetrics {
  private Timer eventPublisherTimer;

  private CdcMonitoringDao cdcMonitoringDao;
  private long replicationLagMeasuringIntervalInMilliseconds;

  private AtomicLong lag = new AtomicLong(-1);
  private Number lagAge = new AtomicLong(-1);
  private AtomicInteger connected = new AtomicInteger(0);

  private long lastTimeEventReceived = -1;

  public DbLogMetrics(Optional<MeterRegistry> meterRegistry,
                      CdcMonitoringDao cdcMonitoringDao,
                      long binlogClientId,
                      long replicationLagMeasuringIntervalInMilliseconds) {

    super(meterRegistry, binlogClientId);

    this.cdcMonitoringDao = cdcMonitoringDao;
    this.replicationLagMeasuringIntervalInMilliseconds = replicationLagMeasuringIntervalInMilliseconds;
    tags = ImmutableList.of(Tag.of("binlogClientId", String.valueOf(binlogClientId)));

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

    lag.set(System.currentTimeMillis() - timestamp);
  }

  public void onBinlogEntryProcessed() {
    meterRegistry.ifPresent(mr -> mr.counter("eventuate.cdc.binlog.entries.processed", tags).increment());
  }

  public void onConnected() {
    connected.set(1);
    meterRegistry.ifPresent(mr -> mr.counter("eventuate.cdc.connection.attempts", tags).increment());
  }

  public void onDisconnected() {
    connected.set(0);
  }

  private void initLagMeasurementTimer() {
    eventPublisherTimer = new Timer();

    eventPublisherTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        cdcMonitoringDao.update(binlogClientId);
      }
    }, 0, replicationLagMeasuringIntervalInMilliseconds);
  }

  private void initMetrics() {
    if (meterRegistry != null) {
      lagAge = new Number() {
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

      meterRegistry.ifPresent(mr -> mr.gauge("eventuate.cdc.replication.lag.age", tags, lagAge));
      meterRegistry.ifPresent(mr -> mr.gauge("eventuate.cdc.replication.lag", tags, lag));
      meterRegistry.ifPresent(mr -> mr.gauge("eventuate.cdc.connected.to.database", tags, connected));
    }
  }
}
