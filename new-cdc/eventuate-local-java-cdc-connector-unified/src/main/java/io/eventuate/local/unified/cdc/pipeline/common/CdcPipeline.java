package io.eventuate.local.unified.cdc.pipeline.common;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.EventTableChangesToAggregateTopicTranslator;

public class CdcPipeline<EVENT extends BinLogEvent> {
  private EventTableChangesToAggregateTopicTranslator<EVENT> publishedEventEventTableChangesToAggregateTopicTranslator;

  public CdcPipeline(EventTableChangesToAggregateTopicTranslator<EVENT> publishedEventEventTableChangesToAggregateTopicTranslator) {
    this.publishedEventEventTableChangesToAggregateTopicTranslator = publishedEventEventTableChangesToAggregateTopicTranslator;
  }

  public void start() {
    publishedEventEventTableChangesToAggregateTopicTranslator.start();
  }

  public void stop() {
    publishedEventEventTableChangesToAggregateTopicTranslator.stop();
  }
}
