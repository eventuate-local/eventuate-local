package io.eventuate.local.common;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;

public class CommonCdcMetrics {
  private MeterRegistry meterRegistry;
  private long binlogClientId;

  private AtomicInteger leader = new AtomicInteger(0);


  public CommonCdcMetrics(MeterRegistry meterRegistry,
                          long binlogClientId) {

    this.meterRegistry = meterRegistry;
    this.binlogClientId = binlogClientId;

    initMetrics();
  }

  public void setLeader(boolean value) {
    leader.set(value ? 1 : 0);
  }

  public void onMessageProcessed() {
    meterRegistry.counter("eventuate.messages.processed." + binlogClientId).increment();
  }

  private void initMetrics() {
    if (meterRegistry == null) {
      return;
    }

    meterRegistry.gauge("eventuate.leader." + binlogClientId, leader);
  }
}
