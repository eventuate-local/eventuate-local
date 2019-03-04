package io.eventuate.local.common;

import io.micrometer.core.instrument.MeterRegistry;

import java.util.concurrent.atomic.AtomicInteger;

public class CommonCdcMetrics extends AbstractCdcMetrics {
  private AtomicInteger leader = new AtomicInteger(0);

  public CommonCdcMetrics(MeterRegistry meterRegistry,
                          String readerName) {

    super(meterRegistry, readerName);

    initMetrics();
  }

  public void setLeader(boolean value) {
    leader.set(value ? 1 : 0);
  }

  public void onMessageProcessed() {
    meterRegistry.counter("eventuate.cdc.messages.processed", tags).increment();
  }

  private void initMetrics() {
    if (meterRegistry == null) {
      return;
    }

    meterRegistry.gauge("eventuate.cdc.leader", tags, leader);
  }
}
