package io.eventuate.local.db.log.test.common;

import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.java.common.broker.CdcDataPublisherTransactionTemplate;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.KafkaCdcDataPublisherTransactionTemplate;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.local.test.util.AbstractConnectorTest;
import io.eventuate.testutil.Eventually;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = KafkaTransactionTemplateTest.Config.class)
public class KafkaTransactionTemplateTest extends AbstractConnectorTest {

  @Configuration
  @EnableConfigurationProperties({EventuateKafkaProducerConfigurationProperties.class,
          EventuateKafkaConsumerConfigurationProperties.class})
  public static class Config {

    @Bean
    public MeterRegistry meterRegistry() {
      return new SimpleMeterRegistry();
    }

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
              eventuateKafkaProducerConfigurationProperties,
              UUID.randomUUID().toString());
    }

    @Bean
    public CdcDataPublisher cdcDataPublisher(EventuateKafkaProducer eventuateKafkaProducer,
                                             MeterRegistry meterRegistry) {

      return new CdcDataPublisher(eventuateKafkaProducer, meterRegistry);
    }

    @Bean
    public CdcDataPublisherTransactionTemplate transactionTemplate(EventuateKafkaProducer eventuateKafkaProducer) {
      return new KafkaCdcDataPublisherTransactionTemplate(eventuateKafkaProducer);
    }
  }

  @Autowired
  EventuateKafkaProducer eventuateKafkaProducer;

  @Autowired
  EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties;

  @Autowired
  CdcDataPublisherTransactionTemplate cdcDataPublisherTransactionTemplate;

  @Test
  public void shouldWorkInTransaction() {
    String topic = "topic-" + UUID.randomUUID().toString();

    cdcDataPublisherTransactionTemplate.inTransaction(() -> {
      eventuateKafkaProducer.send(topic, "key1", "value1");
      eventuateKafkaProducer.send(topic, "key2", "value2");
    });

    KafkaConsumer<String, String> consumer = createConsumer(eventuateKafkaConfigurationProperties.getBootstrapServers());
    consumer.partitionsFor("topic1");
    consumer.subscribe(Collections.singletonList(topic));

    List<ConsumerRecord<String, String>> records = new ArrayList<>();

    Eventually.eventually(10, 300, TimeUnit.MILLISECONDS, () -> {
      ConsumerRecords<String, String> recs = consumer.poll(100);
      recs.forEach(records::add);

      Assert.assertEquals(2, records.size());
      Assert.assertEquals(1, records.stream().filter(r -> r.key().equals("key1") && r.value().equals("value1")).count());
      Assert.assertEquals(1, records.stream().filter(r -> r.key().equals("key2") && r.value().equals("value2")).count());
    });

    consumer.close();
  }

  @Test
  public void shouldAbortTransaction() {
    String topic = "topic-" + UUID.randomUUID().toString();

    try {
      cdcDataPublisherTransactionTemplate.inTransaction(() -> {
        eventuateKafkaProducer.send(topic, "key1", "value1");
        throw new RuntimeException("Transaction Error");
      });
    }
    catch(RuntimeException e) {
      Assert.assertEquals("Transaction Error", e.getMessage());
    }

    KafkaConsumer<String, String> consumer = createConsumer(eventuateKafkaConfigurationProperties.getBootstrapServers());
    consumer.partitionsFor("topic1");
    consumer.subscribe(Collections.singletonList(topic));

    ConsumerRecords<String, String> recs = consumer.poll(3000);

    Assert.assertEquals(0, recs.count());

    consumer.close();
  }
}
