package io.eventuate.local.java.micronaut.jdbc.jdbckafkastore;

import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.example.banking.domain.AccountSnapshotStrategy;
import io.eventuate.sync.AggregateRepository;
import io.eventuate.sync.EventuateAggregateStore;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

import static org.mockito.Mockito.spy;

@Factory
public class JdbcSnapshotsIntegrationTestFactory {

  @Singleton
  public AccountSnapshotStrategy accountSnapshotStrategy() {
    return spy(AccountSnapshotStrategy.class);
  }

  @Singleton
  public AggregateRepository<Account, AccountCommand> accountRepositorySync(EventuateAggregateStore aggregateStore) {
    return new AggregateRepository<>(Account.class, aggregateStore);
  }
}
