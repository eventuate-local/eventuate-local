package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.EntityWithIdAndVersion;
import io.eventuate.EntityWithMetadata;
import io.eventuate.example.banking.domain.*;
import io.eventuate.sync.AggregateRepository;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public abstract class AbstractCommonJdbcAutoConfigurationWithSnapshotsIntegrationTest {

  public void shouldCreateAndUpdateAccounts() throws ExecutionException, InterruptedException {
    AggregateRepository<Account, AccountCommand> accountRepository = getAccountRepository();
    AccountSnapshotStrategy accountSnapshotStrategy = getAccountSnapshotStrategy();

    verify(accountSnapshotStrategy).getAggregateClass();

    BigDecimal initialBalance = new BigDecimal(12345);
    BigDecimal debitAmount = new BigDecimal(12);

    EntityWithIdAndVersion<Account> saveResult = accountRepository.save(new CreateAccountCommand(initialBalance));

    accountRepository.update(saveResult.getEntityId(), new DebitAccountCommand(debitAmount, null));

    verify(accountSnapshotStrategy).possiblySnapshot(any(), any(), any(), any());

    EntityWithMetadata<Account> findResult = accountRepository.find(saveResult.getEntityId());

    assertEquals(initialBalance.subtract(debitAmount), findResult.getEntity().getBalance());

    verify(accountSnapshotStrategy).recreateAggregate(any(), any(), any());

    verifyNoMoreInteractions(accountSnapshotStrategy);
  }

  protected abstract AggregateRepository<Account, AccountCommand> getAccountRepository();
  protected abstract AccountSnapshotStrategy getAccountSnapshotStrategy();
}
