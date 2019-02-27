package io.eventuate.local.java.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * A Kafka consumer that manually commits offsets and supports asynchronous message processing
 */
public class EventuateKafkaConsumer {


  private static Logger logger = LoggerFactory.getLogger(EventuateKafkaConsumer.class);
  private final String subscriberId;
  private final EventuateKafkaConsumerMessageHandler handler;
  private final List<String> topics;
  private AtomicBoolean stopFlag = new AtomicBoolean(false);
  private Properties consumerProperties;
  private volatile EventuateKafkaConsumerState state = EventuateKafkaConsumerState.CREATED;

  public EventuateKafkaConsumer(String subscriberId,
                                EventuateKafkaConsumerMessageHandler handler,
                                List<String> topics,
                                String bootstrapServers,
                                EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
    this.subscriberId = subscriberId;
    this.handler = handler;
    this.topics = topics;

    this.consumerProperties = ConsumerPropertiesFactory.makeDefaultConsumerProperties(bootstrapServers, subscriberId);
    this.consumerProperties.putAll(eventuateKafkaConsumerConfigurationProperties.getProperties());
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
      logger.debug("Committed offsets {}", subscriberId);
      processor.noteOffsetsCommitted(offsetsToCommit);
    }
  }

  public void start() {
    try {

      KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);

      KafkaMessageProcessor processor = new KafkaMessageProcessor(subscriberId, handler);

      for (String topic : topics) {
        verifyTopicExistsBeforeSubscribing(consumer, topic);
      }

      logger.debug("Subscribing to {} {}", subscriberId, topics);

      consumer.subscribe(new ArrayList<>(topics));

      logger.debug("Subscribed to {} {}", subscriberId, topics);

      new Thread(() -> {


        try {
          while (!stopFlag.get()) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            if (!records.isEmpty())
              logger.debug("Got {} {} records", subscriberId, records.count());

            if (records.isEmpty())
              processor.throwFailureException();
            else
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

          state = EventuateKafkaConsumerState.STOPPED;

        } catch (KafkaMessageProcessorFailedException e) {
          // We are done
          logger.trace("Terminating since KafkaMessageProcessorFailedException");
          state = EventuateKafkaConsumerState.MESSAGE_HANDLING_FAILED;
        } catch (Throwable e) {
          logger.error("Got exception: ", e);
          state = EventuateKafkaConsumerState.FAILED;
          throw new RuntimeException(e);
        }
        logger.trace("Stopped in state {}", state);

      }, "Eventuate-subscriber-" + subscriberId).start();

      state = EventuateKafkaConsumerState.STARTED;

    } catch (Exception e) {
      logger.error("Error subscribing", e);
      state = EventuateKafkaConsumerState.FAILED_TO_START;
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    stopFlag.set(true);
  }

  public EventuateKafkaConsumerState getState() {
    return state;
  }
}
