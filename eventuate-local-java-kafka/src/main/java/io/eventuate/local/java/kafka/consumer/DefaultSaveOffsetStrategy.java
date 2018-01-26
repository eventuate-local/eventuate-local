package io.eventuate.local.java.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;

public class DefaultSaveOffsetStrategy implements SaveOffsetStrategy {
  @Override
  public void saveOffsets(KafkaConsumer<String, String> consumer, String subscriberId, Map<TopicPartition, OffsetAndMetadata> offsets) {
    consumer.commitSync(offsets);
  }

  @Override
  public ConsumerRebalanceListener getConsumerRebalanceListener(String subscriberId, KafkaConsumer<String, String> consumer) {
    return new NoOpConsumerRebalanceListener();
  }
}
