package io.eventuate.local.common;

import io.eventuate.local.java.kafka.TopicCleaner;

public class AggregateTopicMapping {

  public static String aggregateTypeToTopic(String aggregateType) {
    return TopicCleaner.clean(aggregateType);
  }

}
