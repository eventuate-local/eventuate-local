package io.eventuate.local.java.jdbckafkastore;

public class DecodedEtopContext {

  public final String id;
  public  final String topic;
  public final int partition;
  public  final long offset;

  public DecodedEtopContext(String id, String topic, int partition, long offset) {

    this.id = id;
    this.topic = topic;
    this.partition = partition;
    this.offset = offset;
  }
}
