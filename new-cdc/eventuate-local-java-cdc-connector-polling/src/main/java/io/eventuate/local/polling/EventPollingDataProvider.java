package io.eventuate.local.polling;

import io.eventuate.local.common.EventuateConstants;
import io.eventuate.local.common.PublishedEvent;

public class EventPollingDataProvider implements PollingDataProvider<PublishedEventBean, PublishedEvent, String> {

  private String table;

  public EventPollingDataProvider() {
    this("eventuate");
  }

  public EventPollingDataProvider(String database) {
    table = EventuateConstants.EMPTY_DATABASE_SCHEMA.equals(database) ? "events" : database + ".events";
  }

  @Override
  public String table() {
    return table;
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
