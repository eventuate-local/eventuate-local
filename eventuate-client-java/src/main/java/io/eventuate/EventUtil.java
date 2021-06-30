package io.eventuate;

import java.util.Arrays;
import java.util.List;

public class EventUtil {

  public static List<Event> events(Event... events) {
    return Arrays.asList(events);
  }
}
