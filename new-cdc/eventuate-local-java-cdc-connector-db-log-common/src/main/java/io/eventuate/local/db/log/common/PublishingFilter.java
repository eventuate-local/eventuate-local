package io.eventuate.local.db.log.common;

import io.eventuate.local.common.BinlogFileOffset;

public interface PublishingFilter {
  boolean shouldBePublished(BinlogFileOffset sourceBinlogFileOffset, String destinationTopic);
}
