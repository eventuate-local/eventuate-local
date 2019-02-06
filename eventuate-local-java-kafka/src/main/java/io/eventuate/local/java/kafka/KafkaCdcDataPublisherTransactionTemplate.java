package io.eventuate.local.java.kafka;

import io.eventuate.local.java.common.broker.CdcDataPublisherTransactionTemplate;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;

public class KafkaCdcDataPublisherTransactionTemplate implements CdcDataPublisherTransactionTemplate {
  private EventuateKafkaProducer eventuateKafkaProducer;

  public KafkaCdcDataPublisherTransactionTemplate(EventuateKafkaProducer eventuateKafkaProducer) {
    this.eventuateKafkaProducer = eventuateKafkaProducer;
  }

  @Override
  public void inTransaction(Runnable code) {
    eventuateKafkaProducer.beginTransaction();
    code.run();
    eventuateKafkaProducer.commitTransaction();
  }
}
