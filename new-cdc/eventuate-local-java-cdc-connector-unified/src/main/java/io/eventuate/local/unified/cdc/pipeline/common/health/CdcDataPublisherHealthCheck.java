package io.eventuate.local.unified.cdc.pipeline.common.health;

import io.eventuate.local.common.CdcDataPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;

import java.util.Collections;
import java.util.List;

public class CdcDataPublisherHealthCheck extends AbstractHealthCheck {

  @Value("${eventuatelocal.cdc.max.event.interval.to.assume.reader.healthy:#{60000}}")
  private long maxEventIntervalToAssumeReaderHealthy;

  private CdcDataPublisher cdcDataPublisher;

  public CdcDataPublisherHealthCheck(CdcDataPublisher cdcDataPublisher) {
    this.cdcDataPublisher = cdcDataPublisher;
  }

  @Override
  protected void determineHealth(HealthBuilder builder) {
    if (cdcDataPublisher.isLastMessagePublishingFailed())
      builder.addError("Last event publishing failed");
  }
}
