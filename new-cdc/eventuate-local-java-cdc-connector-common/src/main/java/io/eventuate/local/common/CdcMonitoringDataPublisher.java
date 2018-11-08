package io.eventuate.local.common;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

public class CdcMonitoringDataPublisher {
  protected Logger logger = LoggerFactory.getLogger(getClass());

  private Timer eventPublisherTimer;

  private MeterRegistry meterRegistry;
  private CdcMonitoringDao cdcMonitoringDao;
  private long binlogClientId;
  private long replicationLagMeasuringIntervalInMilliseconds;

  private AtomicLong lag = new AtomicLong(-1);
  private Number lagAge = new AtomicLong(-1);
  private long lastTimeEventReceived = -1;

  public CdcMonitoringDataPublisher(MeterRegistry meterRegistry,
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

    eventPublisherTimer = new Timer();

    eventPublisherTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        cdcMonitoringDao.update(binlogClientId);
      }
    }, 0, replicationLagMeasuringIntervalInMilliseconds);
  }

  public void eventReceived(long timestamp) {

    if (meterRegistry == null) {
      return;
    }

    lastTimeEventReceived = System.currentTimeMillis();

    lag.set(System.currentTimeMillis() - timestamp);
  }

  public void stop() {
    if (meterRegistry == null) {
      return;
    }

    eventPublisherTimer.cancel();
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
    }
  }
}
