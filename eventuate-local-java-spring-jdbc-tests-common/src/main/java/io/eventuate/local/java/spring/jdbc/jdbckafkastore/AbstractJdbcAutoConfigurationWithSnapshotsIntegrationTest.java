package io.eventuate.local.java.spring.jdbc.jdbckafkastore;

import io.eventuate.example.banking.domain.*;
import io.eventuate.local.java.jdbckafkastore.AbstractCommonJdbcAutoConfigurationWithSnapshotsIntegrationTest;
import io.eventuate.sync.AggregateRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;

public abstract class AbstractJdbcAutoConfigurationWithSnapshotsIntegrationTest extends AbstractCommonJdbcAutoConfigurationWithSnapshotsIntegrationTest {
  @Autowired
  private AggregateRepository<Account, AccountCommand> accountRepository;

  @Autowired
  private AccountSnapshotStrategy accountSnapshotStrategy;

  @Test
  public void shouldCreateAndUpdateAccounts() throws ExecutionException, InterruptedException {
    super.shouldCreateAndUpdateAccounts();
  }

  @Override
  protected AggregateRepository<Account, AccountCommand> getAccountRepository() {
    return accountRepository;
  }

  @Override
  protected AccountSnapshotStrategy getAccountSnapshotStrategy() {
    return accountSnapshotStrategy;
  }
}
