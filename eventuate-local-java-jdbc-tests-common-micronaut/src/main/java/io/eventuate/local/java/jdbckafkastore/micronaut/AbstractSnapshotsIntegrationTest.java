package io.eventuate.local.java.jdbckafkastore.micronaut;

import io.eventuate.example.banking.domain.*;
import io.eventuate.local.java.jdbckafkastore.AbstractCommonJdbcAutoConfigurationWithSnapshotsIntegrationTest;
import io.eventuate.sync.AggregateRepository;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

public abstract class AbstractSnapshotsIntegrationTest extends AbstractCommonJdbcAutoConfigurationWithSnapshotsIntegrationTest {
  @Inject
  private AggregateRepository<Account, AccountCommand> accountRepository;

  @Inject
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
