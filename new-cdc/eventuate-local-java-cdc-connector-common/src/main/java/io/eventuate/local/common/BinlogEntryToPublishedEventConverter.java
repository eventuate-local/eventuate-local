package io.eventuate.local.common;

import java.util.Optional;

public class BinlogEntryToPublishedEventConverter implements BinlogEntryToEventConverter<PublishedEvent> {
  @Override
  public PublishedEvent convert(BinlogEntry binlogEntry) {
    return new PublishedEvent(
            (String)binlogEntry.getColumn("event_id"),
            (String)binlogEntry.getColumn("entity_id"),
            (String)binlogEntry.getColumn("entity_type"),
            (String)binlogEntry.getColumn("event_data"),
            (String)binlogEntry.getColumn("event_type"),
            binlogEntry.getBinlogFileOffset(),
            Optional.ofNullable((String)binlogEntry.getColumn("metadata")));
  }
}