package io.eventuate.local.java.kafka;

import io.eventuate.local.java.common.broker.CdcDataPublisherTransactionTemplate;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.ProducerFencedException;

public class KafkaCdcDataPublisherTransactionTemplate implements CdcDataPublisherTransactionTemplate {
  private EventuateKafkaProducer eventuateKafkaProducer;

  public KafkaCdcDataPublisherTransactionTemplate(EventuateKafkaProducer eventuateKafkaProducer) {
    this.eventuateKafkaProducer = eventuateKafkaProducer;
  }

  @Override
  public void inTransaction(Runnable code) {
    try {
      eventuateKafkaProducer.beginTransaction();
      code.run();
      eventuateKafkaProducer.commitTransaction();
    }
    catch(Exception e) {
      eventuateKafkaProducer.abortTransaction();
      throw e;
    }
  }
}
