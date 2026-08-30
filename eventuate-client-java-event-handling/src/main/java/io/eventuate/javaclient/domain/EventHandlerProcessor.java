package io.eventuate.javaclient.domain;

import java.lang.reflect.AccessibleObject;

public interface EventHandlerProcessor {
  boolean supports(AccessibleObject fieldOrMethod);

  EventHandler process(Object eventHandler, AccessibleObject fieldOrMethod);
}
