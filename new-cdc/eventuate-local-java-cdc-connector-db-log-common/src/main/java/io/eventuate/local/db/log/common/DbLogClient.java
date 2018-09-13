package io.eventuate.local.db.log.common;

public interface DbLogClient {
  void start();
  void stop();
}
