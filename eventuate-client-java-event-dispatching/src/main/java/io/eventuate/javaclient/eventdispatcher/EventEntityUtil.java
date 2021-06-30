package io.eventuate.javaclient.eventdispatcher;

import io.eventuate.Event;
import io.eventuate.EventEntity;
import org.springframework.core.annotation.AnnotationUtils;

public class EventEntityUtil {
  public static Class<?> toEntityType(Class<Event> eventType) {
    String entityName = toEntityTypeName(eventType);
    try {
      return Class.forName(entityName, true, Thread.currentThread().getContextClassLoader());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

  }

  public static String toEntityTypeName(Class<Event> eventType) {
    EventEntity a = AnnotationUtils.findAnnotation(eventType, EventEntity.class);
    if (a == null)
      a = eventType.getPackage().getAnnotation(EventEntity.class);
    if (a == null)
      throw new RuntimeException("Neither this event class " + eventType.getName() + " nor it's package has a EventEntity annotation");

    return a.entity();
  }
}
