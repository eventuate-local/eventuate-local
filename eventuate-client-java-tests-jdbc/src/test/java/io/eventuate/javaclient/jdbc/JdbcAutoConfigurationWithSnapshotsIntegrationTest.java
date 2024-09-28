package io.eventuate.javaclient.jdbc;

import io.eventuate.EntityWithIdAndVersion;
import io.eventuate.EntityWithMetadata;
import io.eventuate.MissingApplyEventMethodStrategy;
import io.eventuate.example.banking.domain.*;
import io.eventuate.sync.AggregateRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = JdbcAutoConfigurationIntegrationWithSnapshotsTestConfiguration.class)
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

    verify(accountSnapshotStrategy).recreateAggregate(any(), any(), any(MissingApplyEventMethodStrategy.class));

    verifyNoMoreInteractions(accountSnapshotStrategy);

  }


}
