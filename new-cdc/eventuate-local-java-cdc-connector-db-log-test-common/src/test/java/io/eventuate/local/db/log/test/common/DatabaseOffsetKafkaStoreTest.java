package io.eventuate.local.db.log.test.common;

import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.local.test.util.AbstractConnectorTest;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DatabaseOffsetKafkaStoreTest.Config.class)
public class DatabaseOffsetKafkaStoreTest extends AbstractConnectorTest {

  @Configuration
  @EnableConfigurationProperties({EventuateKafkaProducerConfigurationProperties.class,
          EventuateKafkaConsumerConfigurationProperties.class})
  public static class Config {
    @Bean
    public EventuateConfigurationProperties eventuateConfigurationProperties() {
      return new EventuateConfigurationProperties();
    }

    @Bean
    public EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties() {
      return new EventuateKafkaConfigurationProperties();
    }

    @Bean
    public EventuateKafkaProducer eventuateKafkaProducer(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                         EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties) {
      return new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(),
              eventuateKafkaProducerConfigurationProperties);
    }
  }

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
    OffsetStore offsetStore = getDatabaseOffsetKafkaStore(UUID.randomUUID().toString(), "mySqlBinaryLogClientName");
    offsetStore.getLastBinlogFileOffset().isPresent();
    offsetStore.stop();
  }

  @Test
  public void shouldWorkCorrectlyWithMultipleDifferentNamedBinlogs() {
    floodTopic(eventuateConfigurationProperties.getOffsetStorageTopicName(), "mySqlBinaryLogClientName1");

    generateAndSaveBinlogFileOffset();
  }

  @Test
  public void shouldReadTheLastRecordMultipleTimes() throws InterruptedException {
    long t = System.currentTimeMillis();
    BinlogFileOffset bfo = generateAndSaveBinlogFileOffset();

    System.out.println((System.currentTimeMillis() - t) / 1000);

    assertLastRecordEquals(bfo);
    System.out.println((System.currentTimeMillis() - t) / 1000);
    assertLastRecordEquals(bfo);
    System.out.println((System.currentTimeMillis() - t) / 1000);
  }

  private void floodTopic(String topicName, String key) {
    Producer<String, String> producer = createProducer(eventuateKafkaConfigurationProperties.getBootstrapServers());
    for (int i = 0; i < 10; i++)
      producer.send(new ProducerRecord<>(topicName, key, Integer.toString(i)));

    producer.close();
  }

  public DatabaseOffsetKafkaStore getDatabaseOffsetKafkaStore(String topicName, String key) {
    return new DatabaseOffsetKafkaStore(topicName,
            key,
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties,
            EventuateKafkaConsumerConfigurationProperties.empty());
  }

  private BinlogFileOffset generateAndSaveBinlogFileOffset() {
    BinlogFileOffset bfo = generateBinlogFileOffset();
    DatabaseOffsetKafkaStore offsetStore = getDatabaseOffsetKafkaStore(eventuateConfigurationProperties.getOffsetStorageTopicName(), "mySqlBinaryLogClientName");
    offsetStore.save(bfo);

    offsetStore.scheduledBinlogFilenameAndOffsetUpdate();

    BinlogFileOffset savedBfo = offsetStore.getLastBinlogFileOffset().get();
    assertEquals(bfo, savedBfo);
    offsetStore.stop();
    return savedBfo;
  }

  private void assertLastRecordEquals(BinlogFileOffset binlogFileOffset) {
    OffsetStore offsetStore = getDatabaseOffsetKafkaStore(eventuateConfigurationProperties.getOffsetStorageTopicName(), "mySqlBinaryLogClientName");

    BinlogFileOffset lastRecord = offsetStore.getLastBinlogFileOffset().get();
    assertEquals(binlogFileOffset, lastRecord);
    offsetStore.stop();
  }
}
