package io.eventuate.local.db.log.common;

import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class OffsetKafkaStore implements OffsetStore {

  private Logger logger = LoggerFactory.getLogger(getClass());

  protected final String dbHistoryTopicName;
  protected EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;
  private EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties;

  private final static int N = 20;

  public OffsetKafkaStore(String offsetStorageName,
                          EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                          EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
    this.eventuateKafkaConfigurationProperties = eventuateKafkaConfigurationProperties;
    this.dbHistoryTopicName = offsetStorageName;
    this.eventuateKafkaConsumerConfigurationProperties = eventuateKafkaConsumerConfigurationProperties;
  }

  @Override
  public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
    try (KafkaConsumer<String, String> consumer = createConsumer()) {
      getPartitionsForTopicRetryOnFail(consumer, 10);
      consumer.subscribe(Arrays.asList(dbHistoryTopicName));

      int count = N;
      BinlogFileOffset result = null;
      boolean lastRecordFound = false;
      while (!lastRecordFound) {
        ConsumerRecords<String, String> records = consumer.poll(100);
        if (records.isEmpty()) {
          count--;
          if (count == 0)
            lastRecordFound = true;
        } else {
          count = N;
          for (ConsumerRecord<String, String> record : records) {
            BinlogFileOffset current = handleRecord(record);
            if (current != null) {
              result = current;
            }
          }
        }
      }
      return Optional.ofNullable(result);
    }
  }

  public List<PartitionInfo> getPartitionsForTopicRetryOnFail(KafkaConsumer<String, String> consumer, int attempts) {
    try {
      return consumer.partitionsFor(dbHistoryTopicName);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      if (attempts > 0) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException ie) {
          throw new RuntimeException(e);
        }
        return getPartitionsForTopicRetryOnFail(consumer, attempts - 1);
      }
      else throw new RuntimeException(e);
    }
  }

  private KafkaConsumer<String, String> createConsumer() {
    Properties props = new Properties();
    props.put("bootstrap.servers", eventuateKafkaConfigurationProperties.getBootstrapServers());
    props.put("auto.offset.reset", "earliest");
    props.put("group.id", UUID.randomUUID().toString());
    props.put("enable.auto.commit", "false");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

    props.putAll(eventuateKafkaConsumerConfigurationProperties.getProperties());

    return new KafkaConsumer<>(props);
  }

  protected abstract BinlogFileOffset handleRecord(ConsumerRecord<String, String> record);
}
