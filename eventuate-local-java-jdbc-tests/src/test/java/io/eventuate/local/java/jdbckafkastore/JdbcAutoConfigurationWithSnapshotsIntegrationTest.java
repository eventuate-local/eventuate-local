package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.EntityWithIdAndVersion;
import io.eventuate.EntityWithMetadata;
import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.example.banking.domain.AccountSnapshotStrategy;
import io.eventuate.example.banking.domain.CreateAccountCommand;
import io.eventuate.example.banking.domain.DebitAccountCommand;
import io.eventuate.sync.AggregateRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JdbcAutoConfigurationIntegrationWithSnapshotsTestConfiguration.class)
@IntegrationTest
public class JdbcAutoConfigurationWithSnapshotsIntegrationTest  {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private AggregateRepository<Account, AccountCommand> accountRepository;

  @Autowired
  private AccountSnapshotStrategy accountSnapshotStrategy;

  @Test
  public void shouldCreateAndUpdateAccounts() throws ExecutionException, InterruptedException {

    verify(accountSnapshotStrategy).getAggregateClass();

    BigDecimal initialBalance = new BigDecimal(12345);
    BigDecimal debitAmount = new BigDecimal(12);

    EntityWithIdAndVersion<Account> saveResult = accountRepository.save(new CreateAccountCommand(initialBalance));

    EntityWithIdAndVersion<Account> updateResult = accountRepository.update(saveResult.getEntityId(), new DebitAccountCommand(debitAmount, null));

    verify(accountSnapshotStrategy).possiblySnapshot(any(), any(), any(), any());

    EntityWithMetadata<Account> findResult = accountRepository.find(saveResult.getEntityId());

    assertEquals(initialBalance.subtract(debitAmount), findResult.getEntity().getBalance());

    verify(accountSnapshotStrategy).recreateAggregate(any(), any(), any());

    verifyNoMoreInteractions(accountSnapshotStrategy);

  }


}
