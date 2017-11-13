package io.eventuate.local.polling;

import io.eventuate.local.common.PublishedEvent;

import java.util.Optional;

public class EventPollingDataProvider implements PollingDataProvider<PublishedEventBean, PublishedEvent, String> {

  private String database;

  public EventPollingDataProvider() {
    this("eventuate");
  }

  public EventPollingDataProvider(String database) {
    this.database = database;
  }

  @Override
  public String table() {
    return database + ".events";
  }

  @Override
  public Class<PublishedEventBean> eventBeanClass() {
    return PublishedEventBean.class;
  }

  @Override
  public String getId(PublishedEvent data) {
    return data.getId();
  }

  @Override
  public String publishedField() {
    return "published";
  }

  @Override
  public String idField() {
    return "event_id";
  }

  @Override
  public PublishedEvent transformEventBeanToEvent(PublishedEventBean eventBean) {
    return new PublishedEvent(eventBean.getEventId(),
      eventBean.getEntityId(),
      eventBean.getEntityType(),
	  eventBean.getEventData(),
	  eventBean.getEventType(),
	  null,
	  eventBean.getMetadataOptional());
  }
}
