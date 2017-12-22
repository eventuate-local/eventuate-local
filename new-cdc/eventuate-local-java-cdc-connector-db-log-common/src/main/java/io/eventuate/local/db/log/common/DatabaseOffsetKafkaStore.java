package io.eventuate.local.db.log.common;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DatabaseOffsetKafkaStore extends OffsetKafkaStore {

  private final String mySqlBinaryLogClientName;

  private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
  private EventuateKafkaProducer eventuateKafkaProducer;

  private Optional<BinlogFileOffset> recordToSave = Optional.empty();

  public DatabaseOffsetKafkaStore(String dbHistoryTopicName,
                                  String mySqlBinaryLogClientName,
                                  EventuateKafkaProducer eventuateKafkaProducer,
                                  EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {

    super(dbHistoryTopicName, eventuateKafkaConfigurationProperties);

    this.mySqlBinaryLogClientName = mySqlBinaryLogClientName;
    this.eventuateKafkaProducer = eventuateKafkaProducer;
    scheduledExecutorService.scheduleAtFixedRate(this::scheduledBinlogFilenameAndOffsetUpdate, 5, 5, TimeUnit.SECONDS);
  }

  public synchronized void scheduledBinlogFilenameAndOffsetUpdate() {
    this.recordToSave.ifPresent(this::store);
    this.recordToSave = Optional.empty();
  }

  public synchronized void save(BinlogFileOffset binlogFileOffset) {
    this.recordToSave = Optional.of(binlogFileOffset);
  }

  @Override
  protected BinlogFileOffset handleRecord(ConsumerRecord<String, String> record) {
    if (record.key().equals(mySqlBinaryLogClientName)) {
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
}
