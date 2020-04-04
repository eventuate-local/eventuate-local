package io.eventuate.javaclient.jdbc;

import io.eventuate.javaclient.commonimpl.EventIdTypeAndData;

public class EventAndTrigger {

  public final EventIdTypeAndData event;
  public final String triggeringEvent;

  public EventAndTrigger(EventIdTypeAndData event, String triggeringEvent) {

    this.event = event;
    this.triggeringEvent = triggeringEvent;
  }
}
