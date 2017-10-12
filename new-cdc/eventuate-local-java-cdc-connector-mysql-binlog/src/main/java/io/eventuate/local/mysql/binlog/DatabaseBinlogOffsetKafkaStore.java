package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DatabaseBinlogOffsetKafkaStore {

  private final String dbHistoryTopicName;
  private final String mySqlBinaryLogClientName;

  private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
  private EventuateKafkaProducer eventuateKafkaProducer;
  private EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;

  private final static int N = 20;

  private Optional<BinlogFileOffset> recordToSave = Optional.empty();

  public DatabaseBinlogOffsetKafkaStore(String dbHistoryTopicName, String mySqlBinaryLogClientName, EventuateKafkaProducer eventuateKafkaProducer, EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {
    this.dbHistoryTopicName = dbHistoryTopicName;
    this.mySqlBinaryLogClientName = mySqlBinaryLogClientName;
    this.eventuateKafkaProducer = eventuateKafkaProducer;
    this.eventuateKafkaConfigurationProperties = eventuateKafkaConfigurationProperties;

    scheduledExecutorService.scheduleAtFixedRate(this::scheduledBinlogFilenameAndOffsetUpdate, 5, 5, TimeUnit.SECONDS);
  }

  public synchronized void scheduledBinlogFilenameAndOffsetUpdate() {
    this.recordToSave.ifPresent(this::store);
    this.recordToSave = Optional.empty();
  }

  public synchronized void save(BinlogFileOffset binlogFileOffset) {
    this.recordToSave = Optional.of(binlogFileOffset);
  }

  public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
    try (KafkaConsumer<String, String> consumer = createConsumer()) {
      consumer.partitionsFor(dbHistoryTopicName);
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
            if (record.key().equals(mySqlBinaryLogClientName)) {
              result = JSonMapper.fromJson(record.value(), BinlogFileOffset.class);
            }
          }
        }
      }
      return Optional.ofNullable(result);
    }
  }

  public synchronized void stop() {
    if (this.recordToSave.isPresent())
      this.store(this.recordToSave.get());
    this.scheduledExecutorService.shutdown();
  }

  private synchronized void store(BinlogFileOffset binlogFileOffset) {
    try {
      eventuateKafkaProducer.send(
              dbHistoryTopicName,
              mySqlBinaryLogClientName,
              JSonMapper.toJson(
                      binlogFileOffset
              )
      ).get();
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (Throwable e) {
      e.printStackTrace();
      throw new RuntimeException(e);
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

    return new KafkaConsumer<>(props);
  }
}
