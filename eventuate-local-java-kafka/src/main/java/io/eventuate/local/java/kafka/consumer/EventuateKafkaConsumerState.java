package io.eventuate.local.java.kafka.consumer;

public enum EventuateKafkaConsumerState {
  MESSAGE_HANDLING_FAILED, STARTED, FAILED_TO_START, STOPPED, FAILED, CREATED
}
