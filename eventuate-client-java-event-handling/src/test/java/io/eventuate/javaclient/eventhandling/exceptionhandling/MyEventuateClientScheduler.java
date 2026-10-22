package io.eventuate.javaclient.eventhandling.exceptionhandling;

class MyEventuateClientScheduler implements EventuateClientScheduler {
  @Override
  public void setTimer(long delayInMilliseconds, Runnable callback) {
    callback.run();
  }
}
