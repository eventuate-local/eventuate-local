package io.eventuate.local.java.spring.jdbc.jdbckafkastore;

import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.local.java.jdbckafkastore.AbstractCommonJdbcEventMetadataAutoConfigurationIntegrationSyncTest;
import io.eventuate.local.java.jdbckafkastore.AccountMetadataEventHandler;
import io.eventuate.sync.AggregateRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractJdbcEventMetadataAutoConfigurationIntegrationSyncTest extends AbstractCommonJdbcEventMetadataAutoConfigurationIntegrationSyncTest {

  @Autowired
  private AggregateRepository<Account, AccountCommand> accountRepository;

  @Autowired
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


