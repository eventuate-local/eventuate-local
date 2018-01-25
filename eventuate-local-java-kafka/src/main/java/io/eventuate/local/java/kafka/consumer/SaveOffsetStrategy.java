package io.eventuate.local.java.kafka.consumer;

import org.apache.kafka.common.TopicPartition;

public interface SaveOffsetStrategy {
  void save(String subsriberId, TopicPartition topicPartition, long offset);
  long load(String subsriberId, TopicPartition topicPartition);
}
