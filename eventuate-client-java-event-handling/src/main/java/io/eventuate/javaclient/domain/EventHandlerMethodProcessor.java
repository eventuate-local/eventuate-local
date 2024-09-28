package io.eventuate.javaclient.domain;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

public abstract class EventHandlerMethodProcessor implements EventHandlerProcessor {

  @Override
  public boolean supports(AccessibleObject fieldOrMethod) {
    return (fieldOrMethod instanceof Method) && supportsMethod((Method)fieldOrMethod);
  }


  @Override
  public EventHandler process(Object eventHandler, AccessibleObject fieldOrMethod) {
    return processMethod(eventHandler, (Method)fieldOrMethod);
  }

  protected abstract boolean supportsMethod(Method method);
  protected abstract EventHandler processMethod(Object eventHandler, Method method);
}
