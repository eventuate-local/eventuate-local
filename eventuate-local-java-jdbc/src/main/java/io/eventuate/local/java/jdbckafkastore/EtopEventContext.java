package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.EventContext;

import java.util.Optional;

public class EtopEventContext {

  public static final String PREFIX = "etpo:";
  public static EventContext make(String id, String topic, int partition, long offset) {
    return new EventContext(String.format("%s%s:%s:%s:%s", PREFIX, id, topic, partition, offset));
  }

  public static Optional<DecodedEtopContext> decode(EventContext te) {
    String triggeringEvent = te.getEventToken();
    return decode(triggeringEvent);
  }

  public static Optional<DecodedEtopContext> decode(String triggeringEvent) {
    if (isEtpoEvent(triggeringEvent)) {
      String[] elements = triggeringEvent.substring(PREFIX.length()).split(":");
      return Optional.of(new DecodedEtopContext(elements[0], elements[1], Integer.parseInt(elements[2]), Long.parseLong(elements[3])));
    } else
      return Optional.empty();
  }

  public static boolean isEtpoEvent(String triggeringEvent) {
    return triggeringEvent.startsWith(PREFIX);
  }
}
