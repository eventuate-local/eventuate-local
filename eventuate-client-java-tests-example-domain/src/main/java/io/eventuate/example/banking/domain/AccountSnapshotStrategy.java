package io.eventuate.example.banking.domain;

import io.eventuate.Aggregate;
import io.eventuate.Aggregates;
import io.eventuate.Event;
import io.eventuate.EventWithMetadata;
import io.eventuate.common.id.Int128;
import io.eventuate.MissingApplyEventMethodStrategy;
import io.eventuate.Snapshot;
import io.eventuate.SnapshotStrategy;

import java.util.List;
import java.util.Optional;

public class AccountSnapshotStrategy implements SnapshotStrategy {

  @Override
  public Class<?> getAggregateClass() {
    return Account.class;
  }

  @Override
  public Optional<Snapshot> possiblySnapshot(Aggregate aggregate, Optional<Int128> snapshotVersion, List<EventWithMetadata> oldEvents, List<Event> newEvents) {
    Account account = (Account) aggregate;
    return Optional.of(new AccountSnapshot(account.getBalance()));
  }

  @Override
  public Aggregate recreateAggregate(Class<?> clasz, Snapshot snapshot, MissingApplyEventMethodStrategy missingApplyEventMethodStrategy) {
    AccountSnapshot accountSnapshot = (AccountSnapshot) snapshot;
    Account aggregate = new Account();
    List<Event> events = aggregate.process(new CreateAccountCommand(accountSnapshot.getBalance()));
    Aggregates.applyEventsToMutableAggregate(aggregate, events, missingApplyEventMethodStrategy);
    return aggregate;
  }
}
