package io.eventuate.local.db.log.test.common;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.DuplicatePublishingDetector;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.test.util.AbstractConnectorTest;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DuplicatePublishingDetectorTest.Config.class)
public class DuplicatePublishingDetectorTest extends AbstractConnectorTest {

  @Configuration
  public static class Config {
    @Bean
    public EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties() {
      return new EventuateKafkaConfigurationProperties();
    }
  }

  @Autowired
  EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;

  @Test
  public void emptyTopicTest() {
    DuplicatePublishingDetector duplicatePublishingDetector = new DuplicatePublishingDetector(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            EventuateKafkaConsumerConfigurationProperties.empty());

    BinlogFileOffset bfo = generateBinlogFileOffset();

    assertTrue(duplicatePublishingDetector.shouldBePublished(bfo, generateUniqueTopicName()));
  }

  @Test
  public void shouldBePublishedTest() {
    String topicName = generateUniqueTopicName();
    String binlogFilename = "binlog.file." + System.currentTimeMillis();
    DuplicatePublishingDetector duplicatePublishingDetector = new DuplicatePublishingDetector(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            EventuateKafkaConsumerConfigurationProperties.empty());

    Producer<String, String> producer = createProducer(eventuateKafkaConfigurationProperties.getBootstrapServers());
    floodTopic(producer, binlogFilename, topicName);
    producer.close();

    assertFalse(duplicatePublishingDetector.shouldBePublished(new BinlogFileOffset(binlogFilename, 1L), topicName));
    assertTrue(duplicatePublishingDetector.shouldBePublished(new BinlogFileOffset(binlogFilename, 10L), topicName));
  }

  @Test
  public void shouldHandlePublishCheckForOldEntires() {
    String topicName = generateUniqueTopicName();
    String binlogFilename = "binlog.file." + System.currentTimeMillis();
    DuplicatePublishingDetector duplicatePublishingDetector = new DuplicatePublishingDetector(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            EventuateKafkaConsumerConfigurationProperties.empty());

    Producer<String, String> producer = createProducer(eventuateKafkaConfigurationProperties.getBootstrapServers());
    floodTopic(producer, binlogFilename, topicName);
    sendOldPublishedEvent(producer, topicName);
    producer.close();

    assertTrue(duplicatePublishingDetector.shouldBePublished(new BinlogFileOffset(binlogFilename, 10L), topicName));
  }

  private void floodTopic(Producer<String, String> producer, String binlogFilename, String topicName) {
    for (int i = 0; i < 10; i++) {
      PublishedEvent publishedEvent = new PublishedEvent();
      publishedEvent.setEntityId(UUID.randomUUID().toString());
      publishedEvent.setBinlogFileOffset(new BinlogFileOffset(binlogFilename, (long)i));
      String json = JSonMapper.toJson(publishedEvent);
      producer.send(
              new ProducerRecord<>(topicName,
                      publishedEvent.getEntityId(),
                      json));

    }

  }

  private void sendOldPublishedEvent(Producer<String, String> producer, String topicName) {
    for (int i = 0; i < 10; i++) {
      PublishedEvent publishedEvent = new PublishedEvent();
      publishedEvent.setEntityId(UUID.randomUUID().toString());
      String json = JSonMapper.toJson(publishedEvent);
      producer.send(
              new ProducerRecord<>(topicName,
                      publishedEvent.getEntityId(),
                      json));
    }
  }

}
