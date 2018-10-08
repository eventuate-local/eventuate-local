package io.eventuate.local.test.util;

import io.eventuate.local.common.PublishedEvent;

public interface CdcProcessorCommon {
  default void onEventSent(PublishedEvent publishedEvent) {}
}
