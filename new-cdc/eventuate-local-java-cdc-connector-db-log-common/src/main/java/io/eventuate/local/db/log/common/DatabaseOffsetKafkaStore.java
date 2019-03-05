package io.eventuate.local.db.log.common;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DatabaseOffsetKafkaStore extends OffsetKafkaStore {
  protected Logger logger = LoggerFactory.getLogger(getClass());

  private final String offsetStoreKey;

  private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
  private EventuateKafkaProducer eventuateKafkaProducer;

  private Optional<BinlogFileOffset> recordToSave = Optional.empty();

  public DatabaseOffsetKafkaStore(String dbHistoryTopicName,
                                  String offsetStoreKey,
                                  EventuateKafkaProducer eventuateKafkaProducer,
                                  EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                  EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    super(dbHistoryTopicName, eventuateKafkaConfigurationProperties, eventuateKafkaConsumerConfigurationProperties);

    this.offsetStoreKey = offsetStoreKey;
    this.eventuateKafkaProducer = eventuateKafkaProducer;
    scheduledExecutorService.scheduleAtFixedRate(this::scheduledBinlogFilenameAndOffsetUpdate, 5, 5, TimeUnit.SECONDS);
  }

  public synchronized void scheduledBinlogFilenameAndOffsetUpdate() {
    this.recordToSave.ifPresent(this::store);
    this.recordToSave = Optional.empty();
  }

  @Override
  public synchronized void save(BinlogFileOffset binlogFileOffset) {
    logger.info("database offset kafka store updated offset to save to {}", binlogFileOffset);
    this.recordToSave = Optional.of(binlogFileOffset);
  }

  @Override
  protected BinlogFileOffset handleRecord(ConsumerRecord<String, String> record) {
    if (record.key().equals(offsetStoreKey)) {
      return JSonMapper.fromJson(record.value(), BinlogFileOffset.class);
    }
    return null;
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
              offsetStoreKey,
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
}
