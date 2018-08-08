package io.eventuate.local.unified.cdc.pipeline;

import io.eventuate.local.common.EventTableChangesToAggregateTopicTranslator;
import io.eventuate.local.common.PublishedEvent;

public class CdcPipeline {
  private EventTableChangesToAggregateTopicTranslator<PublishedEvent> publishedEventEventTableChangesToAggregateTopicTranslator;

  public CdcPipeline(EventTableChangesToAggregateTopicTranslator<PublishedEvent> publishedEventEventTableChangesToAggregateTopicTranslator) {
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
