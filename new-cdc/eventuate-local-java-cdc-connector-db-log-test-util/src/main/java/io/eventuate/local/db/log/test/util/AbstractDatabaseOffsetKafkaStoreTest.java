package io.eventuate.local.db.log.test.util;

import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.test.util.AbstractCdcTest;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class AbstractDatabaseOffsetKafkaStoreTest extends AbstractCdcTest {

  @Autowired
  EventuateKafkaProducer eventuateKafkaProducer;

  @Autowired
  EventuateConfigurationProperties eventuateConfigurationProperties;

  @Autowired
  EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;


  @Test
  public void shouldSendBinlogFilenameAndOffset() throws InterruptedException {
    generateAndSaveBinlogFileOffset();
  }

  @Test
  public void shouldGetEmptyOptionalFromEmptyTopic() {
    DatabaseOffsetKafkaStore databaseOffsetKafkaStore = getDatabaseOffsetKafkaStore(UUID.randomUUID().toString(), "mySqlBinaryLogClientName");
    databaseOffsetKafkaStore.getLastBinlogFileOffset().isPresent();
    databaseOffsetKafkaStore.stop();
  }

  @Test
  public void shouldWorkCorrectlyWithMultipleDifferentNamedBinlogs() throws InterruptedException {
    floodTopic(eventuateConfigurationProperties.getDbHistoryTopicName(), "mySqlBinaryLogClientName1");

    generateAndSaveBinlogFileOffset();
  }

  @Test
  public void shouldReadTheLastRecordMultipleTimes() throws InterruptedException {
    BinlogFileOffset bfo = generateAndSaveBinlogFileOffset();

    assertLastRecordEquals(bfo);
    assertLastRecordEquals(bfo);
  }

  private void floodTopic(String topicName, String key) {
    Producer<String, String> producer = createProducer(eventuateKafkaConfigurationProperties.getBootstrapServers());
    for (int i = 0; i < 10; i++)
      producer.send(new ProducerRecord<>(topicName, key, Integer.toString(i)));

    producer.close();
  }

  public DatabaseOffsetKafkaStore getDatabaseOffsetKafkaStore(String topicName, String key) {
    return new DatabaseOffsetKafkaStore(topicName, key, eventuateKafkaProducer, eventuateKafkaConfigurationProperties);
  }

  private BinlogFileOffset generateAndSaveBinlogFileOffset() throws InterruptedException {
    BinlogFileOffset bfo = generateBinlogFileOffset();
    DatabaseOffsetKafkaStore binlogOffsetKafkaStore = getDatabaseOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(), "mySqlBinaryLogClientName");
    binlogOffsetKafkaStore.save(bfo);

    Thread.sleep(5000);

    BinlogFileOffset savedBfo = binlogOffsetKafkaStore.getLastBinlogFileOffset().get();
    assertEquals(bfo, savedBfo);
    binlogOffsetKafkaStore.stop();
    return savedBfo;
  }

  private void assertLastRecordEquals(BinlogFileOffset binlogFileOffset) {
    DatabaseOffsetKafkaStore binlogOffsetKafkaStore = getDatabaseOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(), "mySqlBinaryLogClientName");

    BinlogFileOffset lastRecord = binlogOffsetKafkaStore.getLastBinlogFileOffset().get();
    assertEquals(binlogFileOffset, lastRecord);
    binlogOffsetKafkaStore.stop();
  }
}
