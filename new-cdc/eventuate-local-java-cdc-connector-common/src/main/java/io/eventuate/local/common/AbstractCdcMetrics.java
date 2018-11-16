package io.eventuate.local.common;

import com.google.common.collect.ImmutableList;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.List;

public class AbstractCdcMetrics {
  protected MeterRegistry meterRegistry;
  protected List<Tag> tags;
  protected long binlogClientId;

  public AbstractCdcMetrics(MeterRegistry meterRegistry,
                            long binlogClientId) {

    this.meterRegistry = meterRegistry;
    this.binlogClientId = binlogClientId;
    tags = ImmutableList.of(Tag.of("binlogClientId", String.valueOf(binlogClientId)));
  }
}
