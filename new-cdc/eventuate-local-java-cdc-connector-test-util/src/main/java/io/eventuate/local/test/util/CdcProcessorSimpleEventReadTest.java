package io.eventuate.local.test.util;

import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class CdcProcessorSimpleEventReadTest extends AbstractCdcTest implements CdcProcessorCommon {

  @Test
  public void shouldReadPublishedEvent() throws InterruptedException {
    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    CdcProcessor<PublishedEvent> cdcProcessor = createCdcProcessor();

    cdcProcessor.start(publishedEvent -> {
      publishedEvents.add(publishedEvent);
      onEventSent(publishedEvent);
    });

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(accountCreatedEventData);
    waitForEvent(publishedEvents, entityIdVersionAndEventIds.getEntityVersion(), LocalDateTime.now().plusSeconds(10), accountCreatedEventData);

    cdcProcessor.stop();
  }
}
