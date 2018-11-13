package io.eventuate.local.db.log.common;

import io.eventuate.local.common.CdcMonitoringDao;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DbLogMetrics {
  private Timer eventPublisherTimer;

  private MeterRegistry meterRegistry;
  private CdcMonitoringDao cdcMonitoringDao;
  private long binlogClientId;
  private long replicationLagMeasuringIntervalInMilliseconds;

  private AtomicLong lag = new AtomicLong(-1);
  private Number lagAge = new AtomicLong(-1);
  private AtomicInteger connected = new AtomicInteger(0);
  private AtomicLong connectionAttempts = new AtomicLong(0);

  private long lastTimeEventReceived = -1;

  public DbLogMetrics(MeterRegistry meterRegistry,
                      CdcMonitoringDao cdcMonitoringDao,
                      long binlogClientId,
                      long replicationLagMeasuringIntervalInMilliseconds) {

    this.meterRegistry = meterRegistry;
    this.cdcMonitoringDao = cdcMonitoringDao;
    this.binlogClientId = binlogClientId;
    this.replicationLagMeasuringIntervalInMilliseconds = replicationLagMeasuringIntervalInMilliseconds;

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

  public void onMessageProcessed() {
    meterRegistry.counter("eventuate.messages.processed." + binlogClientId).increment();
  }

  public void onBinlogEntryProcessed() {
    meterRegistry.counter("eventuate.binlog.entries.processed." + binlogClientId).increment();
  }

  public void onConnected() {
    connected.set(1);
    meterRegistry.counter("eventuate.connection.attempts." + binlogClientId).increment();
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

      meterRegistry.gauge("eventuate.replication.lag.age." + binlogClientId, lagAge);
      meterRegistry.gauge("eventuate.replication.lag." + binlogClientId, lag);
      meterRegistry.gauge("eventuate.connected.to.database." + binlogClientId, connected);
    }
  }
}
