package io.eventuate.local.java.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;
import java.util.Optional;

public interface SaveOffsetStrategy {
  void saveOffsets(KafkaConsumer<String, String> consumer, String subscriberId, Map<TopicPartition, OffsetAndMetadata> offsets);
  ConsumerRebalanceListener getConsumerRebalanceListener(String subscriberId, KafkaConsumer<String, String> consumer);
}
