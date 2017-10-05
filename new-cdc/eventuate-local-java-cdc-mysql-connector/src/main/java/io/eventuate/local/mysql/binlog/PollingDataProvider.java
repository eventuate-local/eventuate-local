package io.eventuate.local.mysql.binlog;

public interface PollingDataProvider<EVENT_BEAN, EVENT, ID> {
  String table();
  Class<EVENT_BEAN> eventBeanClass();
  ID getId(EVENT event);
  String publishedField();
  String idField();
  EVENT transformEventBeanToEvent(EVENT_BEAN eventBean);
}
