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

  private final String dbLogClientName;

  private EventuateKafkaProducer eventuateKafkaProducer;

  public DatabaseOffsetKafkaStore(String dbHistoryTopicName,
                                  String dbLogClientName,
                                  EventuateKafkaProducer eventuateKafkaProducer,
                                  EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                  EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    super(dbHistoryTopicName, eventuateKafkaConfigurationProperties, eventuateKafkaConsumerConfigurationProperties);

    this.dbLogClientName = dbLogClientName;
    this.eventuateKafkaProducer = eventuateKafkaProducer;
  }

  @Override
  public synchronized void save(BinlogFileOffset binlogFileOffset) {
    logger.info("database offset kafka store is saving offset {}", binlogFileOffset);
    store(binlogFileOffset);
  }

  @Override
  protected BinlogFileOffset handleRecord(ConsumerRecord<String, String> record) {
    if (record.key().equals(dbLogClientName)) {
      return JSonMapper.fromJson(record.value(), BinlogFileOffset.class);
    }
    return null;
  }

  private synchronized void store(BinlogFileOffset binlogFileOffset) {
    try {
      eventuateKafkaProducer.send(
              dbHistoryTopicName,
              dbLogClientName,
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
