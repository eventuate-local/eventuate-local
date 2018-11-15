package io.eventuate.local.common;

import com.google.common.collect.ImmutableList;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.List;
import java.util.Optional;

public class AbstractCdcMetrics {
  protected Optional<MeterRegistry> meterRegistry;
  protected List<Tag> tags;
  protected long binlogClientId;

  public AbstractCdcMetrics(Optional<MeterRegistry> meterRegistry,
                            long binlogClientId) {

    this.meterRegistry = meterRegistry;
    this.binlogClientId = binlogClientId;
    tags = ImmutableList.of(Tag.of("binlogClientId", String.valueOf(binlogClientId)));
  }
}
