package io.eventuate.local.java.jdbckafkastore.micronaut;

import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.local.java.jdbckafkastore.AccountMetadataEventHandler;
import io.eventuate.sync.AggregateRepository;
import io.eventuate.sync.EventuateAggregateStore;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class JdbcEventMetadataIntegrationSyncTestFactory {

  @Singleton
  public AggregateRepository<Account, AccountCommand> syncAccountRepository(EventuateAggregateStore aggregateStore) {
    return new AggregateRepository<>(Account.class, aggregateStore);
  }

  @Singleton
  public AccountMetadataEventHandler accountMetadataEventHandler() {
    return new AccountMetadataEventHandler();
  }
}
