package io.eventuate.local.polling;

public interface PollingDataProvider {
  String publishedField();
  String idField();
}
