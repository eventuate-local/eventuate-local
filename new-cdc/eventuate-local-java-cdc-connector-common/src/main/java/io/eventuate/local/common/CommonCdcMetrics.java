package io.eventuate.local.common;

import io.micrometer.core.instrument.MeterRegistry;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class CommonCdcMetrics extends AbstractCdcMetrics {
  private AtomicInteger leader = new AtomicInteger(0);

  public CommonCdcMetrics(Optional<MeterRegistry> meterRegistry,
                          long binlogClientId) {

    super(meterRegistry, binlogClientId);

    initMetrics();
  }

  public void setLeader(boolean value) {
    leader.set(value ? 1 : 0);
  }

  public void onMessageProcessed() {
    meterRegistry.ifPresent(mr -> mr.counter("eventuate.cdc.messages.processed", tags).increment());
  }

  private void initMetrics() {
    if (meterRegistry == null) {
      return;
    }

    meterRegistry.ifPresent(mr -> mr.gauge("eventuate.cdc.leader", tags, leader));
  }
}
