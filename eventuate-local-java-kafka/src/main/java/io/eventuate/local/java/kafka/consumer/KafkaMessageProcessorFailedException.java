package io.eventuate.local.java.kafka.consumer;

public class KafkaMessageProcessorFailedException extends RuntimeException {
  public KafkaMessageProcessorFailedException(Throwable t) {
    super(t);
  }
}
