package io.eventuate.local.java.kafka.consumer;

import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.util.test.async.Eventually;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EventuateKafkaConsumerTest.EventuateKafkaConsumerTestConfiguration.class)
public class EventuateKafkaConsumerTest {

  @Configuration
  @EnableAutoConfiguration
  @EnableConfigurationProperties({EventuateKafkaConsumerConfigurationProperties.class, EventuateKafkaProducerConfigurationProperties.class})
  public static class EventuateKafkaConsumerTestConfiguration {

    @Bean
    public EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties() {
      return new EventuateKafkaConfigurationProperties();
    }

    @Bean
    public EventuateKafkaProducer producer(EventuateKafkaConfigurationProperties kafkaProperties, EventuateKafkaProducerConfigurationProperties producerProperties) {
      return new EventuateKafkaProducer(kafkaProperties.getBootstrapServers(), producerProperties);
    }

  }

  @Autowired
  private EventuateKafkaConfigurationProperties kafkaProperties;

  @Autowired
  private EventuateKafkaConsumerConfigurationProperties consumerProperties;

  @Autowired
  private EventuateKafkaProducer producer;

  @Test
  public void shouldStopWhenHandlerThrowsException() {
    String subscriberId = "subscriber-" + System.currentTimeMillis();
    String topic = "topic-" + System.currentTimeMillis();

    EventuateKafkaConsumerMessageHandler handler = makeExceptionThrowingHandler();

    EventuateKafkaConsumer consumer = makeConsumer(subscriberId, topic, handler);

    sendMessages(topic);

    assertConsumerStopped(consumer);

    assertHandlerInvokedAtLeastOnce(handler);
  }

  private EventuateKafkaConsumer makeConsumer(String subscriberId, String topic, EventuateKafkaConsumerMessageHandler handler) {
    EventuateKafkaConsumer consumer = new EventuateKafkaConsumer(subscriberId, handler, Collections.singletonList(topic), kafkaProperties.getBootstrapServers(), consumerProperties);

    consumer.start();
    return consumer;
  }

  private void sendMessages(String topic) {
    producer.send(topic, "1", "a");
    producer.send(topic, "1", "b");
  }

  private void assertHandlerInvokedAtLeastOnce(EventuateKafkaConsumerMessageHandler handler) {
    verify(handler, atLeast(1)).accept(any(), any());
  }

  private EventuateKafkaConsumerMessageHandler makeExceptionThrowingHandler() {
    EventuateKafkaConsumerMessageHandler handler = mock(EventuateKafkaConsumerMessageHandler.class);

    doAnswer(invocation -> {
      CompletableFuture.runAsync(() -> ((BiConsumer<Void, Throwable>)invocation.getArguments()[1]).accept(null, new RuntimeException()));
      return null;
    }).when(handler).accept(any(), any());
    return handler;
  }

  private void assertConsumerStopped(EventuateKafkaConsumer consumer) {
    Eventually.eventually(() -> {
      assertEquals(EventuateKafkaConsumerState.MESSAGE_HANDLING_FAILED, consumer.getState());
    });
  }

}