package io.eventuate.local.polling;

public class EventPollingDataProvider implements PollingDataProvider {

  @Override
  public String publishedField() {
    return "published";
  }

  @Override
  public String idField() {
    return "event_id";
  }
}
