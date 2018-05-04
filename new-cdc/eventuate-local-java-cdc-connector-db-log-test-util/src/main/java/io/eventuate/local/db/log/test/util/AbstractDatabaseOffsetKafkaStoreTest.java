package io.eventuate.local.db.log.test.util;

import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.OffsetStore;
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
    OffsetStore offsetStore = getOffsetStore(UUID.randomUUID().toString(), "mySqlBinaryLogClientName");
    offsetStore.getLastBinlogFileOffset().isPresent();
    offsetStore.stop();
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

  public OffsetStore getOffsetStore(String topicName, String key) {
    return new DatabaseOffsetKafkaStore(topicName, key, eventuateKafkaProducer, eventuateKafkaConfigurationProperties);
  }

  private BinlogFileOffset generateAndSaveBinlogFileOffset() throws InterruptedException {
    BinlogFileOffset bfo = generateBinlogFileOffset();
    OffsetStore offsetStore = getOffsetStore(eventuateConfigurationProperties.getDbHistoryTopicName(), "mySqlBinaryLogClientName");
    offsetStore.save(bfo);

    Thread.sleep(5000);

    BinlogFileOffset savedBfo = offsetStore.getLastBinlogFileOffset().get();
    assertEquals(bfo, savedBfo);
    offsetStore.stop();
    return savedBfo;
  }

  private void assertLastRecordEquals(BinlogFileOffset binlogFileOffset) {
    OffsetStore offsetStore = getOffsetStore(eventuateConfigurationProperties.getDbHistoryTopicName(), "mySqlBinaryLogClientName");

    BinlogFileOffset lastRecord = offsetStore.getLastBinlogFileOffset().get();
    assertEquals(binlogFileOffset, lastRecord);
    offsetStore.stop();
  }
}
