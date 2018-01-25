package io.eventuate.local.java.kafka.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * A Kafka consumer that manually commits offsets and supports asynchronous message processing
 */
public class EventuateKafkaConsumer {


  private static Logger logger = LoggerFactory.getLogger(EventuateKafkaConsumer.class);
  private final String subscriberId;
  private final BiConsumer<ConsumerRecord<String, String>, BiConsumer<Void, Throwable>> handler;
  private final List<String> topics;
  private AtomicBoolean stopFlag = new AtomicBoolean(false);
  private Properties consumerProperties;
  private KafkaConsumer<String, String> consumer;
  private Optional<SaveOffsetStrategy> saveOffsetStrategy;

  public EventuateKafkaConsumer(String subscriberId,
                                BiConsumer<ConsumerRecord<String, String>, BiConsumer<Void, Throwable>> handler,
                                List<String> topics,
                                String bootstrapServers) {
    this(subscriberId, handler, topics, bootstrapServers, Optional.empty());
  }

  public EventuateKafkaConsumer(String subscriberId,
                                BiConsumer<ConsumerRecord<String, String>, BiConsumer<Void, Throwable>> handler,
                                List<String> topics,
                                String bootstrapServers,
                                SaveOffsetStrategy saveOffsetStrategy) {
    this(subscriberId, handler, topics, bootstrapServers, Optional.of(saveOffsetStrategy));
  }

  public EventuateKafkaConsumer(String subscriberId,
                                BiConsumer<ConsumerRecord<String, String>, BiConsumer<Void, Throwable>> handler,
                                List<String> topics,
                                String bootstrapServers,
                                Optional<SaveOffsetStrategy> saveOffsetStrategy) {
    this.subscriberId = subscriberId;
    this.handler = handler;
    this.topics = topics;
    this.saveOffsetStrategy = saveOffsetStrategy;

    this.consumerProperties = ConsumerPropertiesFactory.makeConsumerProperties(bootstrapServers, subscriberId);
  }

  public static List<PartitionInfo> verifyTopicExistsBeforeSubscribing(KafkaConsumer<String, String> consumer, String topic) {
    try {
      logger.debug("Verifying Topic {}", topic);
      List<PartitionInfo> partitions = consumer.partitionsFor(topic);
      logger.debug("Got these partitions {} for Topic {}", partitions, topic);
      return partitions;
    } catch (Throwable e) {
      logger.error("Got exception: ", e);
      throw new RuntimeException(e);
    }
  }

  private void maybeCommitOffsets(KafkaConsumer<String, String> consumer, KafkaMessageProcessor processor) {
    Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = processor.offsetsToCommit();
    if (!offsetsToCommit.isEmpty()) {
      logger.debug("Committing offsets {} {}", subscriberId, offsetsToCommit);
      consumer.commitSync(offsetsToCommit);
      saveOffsetStrategy.ifPresent(strategy -> {
        for (TopicPartition partition : offsetsToCommit.keySet()) {
          strategy.save(subscriberId, partition, offsetsToCommit.get(partition).offset());
        }
      });
      logger.debug("Committed offsets {}", subscriberId);
      processor.noteOffsetsCommitted(offsetsToCommit);
    }
  }

  public String getSubscriberId() {
    return subscriberId;
  }

  public void start() {
    try {

      consumer = new KafkaConsumer<>(consumerProperties);

      KafkaMessageProcessor processor = new KafkaMessageProcessor(subscriberId, handler);

      for (String topic : topics) {
        verifyTopicExistsBeforeSubscribing(consumer, topic);
      }

      logger.debug("Subscribing to {} {}", subscriberId, topics);

      List<String> topicList = new ArrayList<>(topics);

      if (!saveOffsetStrategy.isPresent()) {
        consumer.subscribe(topicList);
      } else {
        consumer.subscribe(topicList, new ConsumerRebalanceListener() {
          @Override
          public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            partitions.forEach(part -> saveOffsetStrategy.get().save(subscriberId, part, consumer.position(part)));
          }

          @Override
          public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            partitions.forEach(part -> consumer.seek(part, saveOffsetStrategy.get().load(subscriberId, part)));
          }
        });
      }

      logger.debug("Subscribed to {} {}", subscriberId, topics);

      new Thread(() -> {


        try {
          while (!stopFlag.get()) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            if (!records.isEmpty())
              logger.debug("Got {} {} records", subscriberId, records.count());

            for (ConsumerRecord<String, String> record : records) {
              logger.debug("processing record {} {} {}", subscriberId, record.offset(), record.value());
              if (logger.isDebugEnabled())
                logger.debug(String.format("EventuateKafkaAggregateSubscriptions subscriber = %s, offset = %d, key = %s, value = %s", subscriberId, record.offset(), record.key(), record.value()));
              processor.process(record);
            }
            if (!records.isEmpty())
              logger.debug("Processed {} {} records", subscriberId, records.count());

            maybeCommitOffsets(consumer, processor);

            if (!records.isEmpty())
              logger.debug("To commit {} {}", subscriberId, processor.getPending());

          }

          maybeCommitOffsets(consumer, processor);
          consumer.close();

        } catch (Throwable e) {
          logger.error("Got exception: ", e);
          throw new RuntimeException(e);
        }

      }, "Eventuate-subscriber-" + subscriberId).start();

    } catch (Exception e) {
      logger.error("Error subscribing", e);
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    stopFlag.set(true);
  }

}
