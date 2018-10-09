package io.eventuate.local.common;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.PublishingFilter;
import io.eventuate.local.java.kafka.consumer.ConsumerPropertiesFactory;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumer;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

public class DuplicatePublishingDetector implements PublishingFilter {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private Map<String, Optional<BinlogFileOffset>> maxOffsetsForTopics = new HashMap<>();
  private boolean okToProcess = false;
  private String kafkaBootstrapServers;
  private EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties;

  public DuplicatePublishingDetector(String kafkaBootstrapServers,
                                     EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    this.eventuateKafkaConsumerConfigurationProperties = eventuateKafkaConsumerConfigurationProperties;
  }

  @Override
  public boolean shouldBePublished(BinlogFileOffset sourceBinlogFileOffset, String destinationTopic) {
    if (okToProcess)
      return true;

    Optional<BinlogFileOffset> max = maxOffsetsForTopics.computeIfAbsent(destinationTopic, this::fetchMaxOffsetFor);
    logger.info("For topic {} max is {}", destinationTopic, max);

    okToProcess = max.map(sourceBinlogFileOffset::isSameOrAfter).orElse(true);

    logger.info("max = {}, sourceBinlogFileOffset = {} okToProcess = {}", max, sourceBinlogFileOffset, okToProcess);
    return okToProcess;
  }

  private Optional<BinlogFileOffset> fetchMaxOffsetFor(String destinationTopic) {
    String subscriberId = "duplicate-checker-" + destinationTopic + "-" + System.currentTimeMillis();
    Properties consumerProperties = ConsumerPropertiesFactory.makeDefaultConsumerProperties(kafkaBootstrapServers, subscriberId);
    consumerProperties.putAll(eventuateKafkaConsumerConfigurationProperties.getProperties());
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);

    List<PartitionInfo> partitions = EventuateKafkaConsumer.verifyTopicExistsBeforeSubscribing(consumer, destinationTopic);

    List<TopicPartition> topicPartitionList = partitions.stream().map(p -> new TopicPartition(destinationTopic, p.partition())).collect(toList());
    consumer.assign(topicPartitionList);
    consumer.poll(0);

    logger.info("Seeking to end");

    try {
      consumer.seekToEnd(topicPartitionList);
    } catch (IllegalStateException e) {
      logger.error("Error seeking " + destinationTopic, e);
      return Optional.empty();
    }
    List<PartitionOffset> positions = topicPartitionList.stream()
            .map(tp -> new PartitionOffset(tp.partition(), consumer.position(tp) - 1))
            .filter(po -> po.offset >= 0)
            .collect(toList());

    logger.info("Seeking to positions=" + positions);

    positions.forEach(po -> {
      consumer.seek(new TopicPartition(destinationTopic, po.partition), po.offset);
    });

    logger.info("Polling for records");

    List<ConsumerRecord<String, String>> records = new ArrayList<>();
    while (records.size()<positions.size()) {
      ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);
      consumerRecords.forEach(records::add);
    }

    logger.info("Got records: {}", records.size());
    Optional<BinlogFileOffset> max =
            records
                    .stream()
                    .map(record -> {
                      logger.info(String.format("got record: %s %s %s", record.partition(), record.offset(), record.value()));
                      return JSonMapper.fromJson(record.value(), PublishedEvent.class).getBinlogFileOffset();
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .max((blfo1, blfo2) -> blfo1.isSameOrAfter(blfo2) ? 1 : -1);
    consumer.close();
    return max;
  }

  class PartitionOffset {

    public final int partition;
    public final long offset;

    @Override
    public String toString() {
      return "PartitionOffset{" +
              "partition=" + partition +
              ", offset=" + offset +
              '}';
    }

    public PartitionOffset(int partition, long offset) {

      this.partition = partition;
      this.offset = offset;
    }
  }
}
