package io.eventuate.local.publisher.changes;

import org.apache.commons.lang.builder.ToStringBuilder;

public class EventInfo {

  private String event_id;
  private String event_type;
  private String event_data;
  private String entity_type;
  private String entity_id;

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public String getEvent_id() {
    return event_id;
  }

  public void setEvent_id(String event_id) {
    this.event_id = event_id;
  }

  public String getEvent_type() {
    return event_type;
  }

  public void setEvent_type(String event_type) {
    this.event_type = event_type;
  }

  public String getEvent_data() {
    return event_data;
  }

  public void setEvent_data(String event_data) {
    this.event_data = event_data;
  }

  public String getEntity_type() {
    return entity_type;
  }

  public void setEntity_type(String entity_type) {
    this.entity_type = entity_type;
  }

  public String getEntity_id() {
    return entity_id;
  }

  public void setEntity_id(String entity_id) {
    this.entity_id = entity_id;
  }
}
