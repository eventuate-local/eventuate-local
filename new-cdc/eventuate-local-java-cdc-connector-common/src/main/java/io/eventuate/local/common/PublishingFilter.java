package io.eventuate.local.common;

public interface PublishingFilter {
  boolean shouldBePublished(BinlogFileOffset sourceBinlogFileOffset, String destinationTopic);
}
