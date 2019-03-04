package io.eventuate.local.common;

import com.google.common.collect.ImmutableList;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.List;

public class AbstractCdcMetrics {
  protected MeterRegistry meterRegistry;
  protected List<Tag> tags;
  protected String readerName;

  public AbstractCdcMetrics(MeterRegistry meterRegistry,
                            String readerName) {

    this.meterRegistry = meterRegistry;
    this.readerName = readerName;
    tags = ImmutableList.of(Tag.of("readerName", readerName));
  }
}
