package io.eventuate.local.java.jdbckafkastore;


import io.eventuate.EventHandlerContext;
import io.eventuate.EventHandlerMethod;
import io.eventuate.EventSubscriber;
import io.eventuate.example.banking.domain.AccountCreatedEvent;
import io.eventuate.example.banking.domain.AccountDebitedEvent;
import io.eventuate.example.banking.services.EventTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventSubscriber(id="accountMetadataEventHandler")
public class AccountMetadataEventHandler {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventTracker<EventHandlerContext<?>> events = EventTracker.create();

  public EventTracker<EventHandlerContext<?>> getEvents() {
    return events;
  }

  @EventHandlerMethod
  public void accountCreated(EventHandlerContext<AccountCreatedEvent> ctx) {
    events.onNext(ctx);
  }

  @EventHandlerMethod
  public void accountDebited(EventHandlerContext<AccountDebitedEvent> ctx) {
    events.onNext(ctx);
  }

}

