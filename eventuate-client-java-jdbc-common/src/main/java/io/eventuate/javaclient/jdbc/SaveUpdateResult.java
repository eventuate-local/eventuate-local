package io.eventuate.javaclient.jdbc;


import io.eventuate.javaclient.commonimpl.crud.EntityIdVersionAndEventIds;

public class SaveUpdateResult  {
  private final EntityIdVersionAndEventIds entityIdVersionAndEventIds;
  private final PublishableEvents publishableEvents;

  public SaveUpdateResult(EntityIdVersionAndEventIds entityIdVersionAndEventIds, PublishableEvents publishableEvents) {
    this.entityIdVersionAndEventIds = entityIdVersionAndEventIds;
    this.publishableEvents = publishableEvents;
  }

  public EntityIdVersionAndEventIds getEntityIdVersionAndEventIds() {
    return entityIdVersionAndEventIds;
  }

  public PublishableEvents getPublishableEvents() {
    return publishableEvents;
  }
}
