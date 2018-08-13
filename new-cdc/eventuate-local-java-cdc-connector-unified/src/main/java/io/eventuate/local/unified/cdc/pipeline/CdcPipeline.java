package io.eventuate.local.unified.cdc.pipeline;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.EventTableChangesToAggregateTopicTranslator;
import io.eventuate.local.common.PublishedEvent;

public class CdcPipeline<EVENT extends BinLogEvent> {
  private EventTableChangesToAggregateTopicTranslator<EVENT> publishedEventEventTableChangesToAggregateTopicTranslator;

  public CdcPipeline(EventTableChangesToAggregateTopicTranslator<EVENT> publishedEventEventTableChangesToAggregateTopicTranslator) {
    this.publishedEventEventTableChangesToAggregateTopicTranslator = publishedEventEventTableChangesToAggregateTopicTranslator;
  }

  public void start() {
    publishedEventEventTableChangesToAggregateTopicTranslator.start();
  }

  public void stop() {
    try {
      publishedEventEventTableChangesToAggregateTopicTranslator.stop();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
