package io.eventuate.local.java.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.function.BiConsumer;

public interface EventuateKafkaConsumerMessageHandler extends BiConsumer<ConsumerRecord<String, String>, BiConsumer<Void, Throwable>> {
}
