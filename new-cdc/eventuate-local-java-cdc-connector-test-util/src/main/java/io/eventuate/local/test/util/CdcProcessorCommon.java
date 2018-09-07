package io.eventuate.local.test.util;

import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;

public interface CdcProcessorCommon {

  CdcProcessor<PublishedEvent> createCdcProcessor();

  default void onEventSent(PublishedEvent publishedEvent) {}
}
