package io.eventuate.local.java.micronaut.jdbc.jdbckafkastore;

import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.local.java.jdbckafkastore.AbstractCommonJdbcEventMetadataAutoConfigurationIntegrationSyncTest;
import io.eventuate.local.java.jdbckafkastore.AccountMetadataEventHandler;
import io.eventuate.sync.AggregateRepository;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

public abstract class AbstractJdbcEventMetadataIntegrationSyncTest extends AbstractCommonJdbcEventMetadataAutoConfigurationIntegrationSyncTest {

  @Inject
  private AggregateRepository<Account, AccountCommand> accountRepository;

  @Inject
  private AccountMetadataEventHandler accountMetadataEventHandler;

  @Test
  public void shouldCreateAccount() {
    super.shouldCreateAccount();
  }

  @Override
  protected AggregateRepository<Account, AccountCommand> getAccountRepository() {
    return accountRepository;
  }

  @Override
  protected AccountMetadataEventHandler getAccountMetadataEventHandler() {
    return accountMetadataEventHandler;
  }
}


