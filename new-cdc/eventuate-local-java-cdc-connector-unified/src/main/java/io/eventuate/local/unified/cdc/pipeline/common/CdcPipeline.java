package io.eventuate.local.unified.cdc.pipeline.common;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.CdcDataPublisher;

public class CdcPipeline<EVENT extends BinLogEvent> {
  private CdcDataPublisher<EVENT> cdcDataPublisher;

  public CdcPipeline(CdcDataPublisher<EVENT> cdcDataPublisher) {
    this.cdcDataPublisher = cdcDataPublisher;
  }

  public void start() {
    cdcDataPublisher.start();
  }

  public void stop() {
    cdcDataPublisher.stop();
  }
}
