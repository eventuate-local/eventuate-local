package io.eventuate.javaclient.spring.tests.common;

import io.eventuate.EntityNotFoundException;
import io.eventuate.example.banking.services.AccountCommandSideEventHandler;
import io.eventuate.example.banking.services.AccountQuerySideEventHandler;
import io.eventuate.example.banking.services.MoneyTransferCommandSideEventHandler;
import io.eventuate.javaclient.tests.common.AbstractAccountIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;

public abstract class AbstractSpringAccountIntegrationTest extends AbstractAccountIntegrationTest {
  @Autowired
  private AccountCommandSideEventHandler accountCommandSideEventHandler;

  @Autowired
  private AccountQuerySideEventHandler accountQuerySideEventHandler;

  @Autowired
  private MoneyTransferCommandSideEventHandler moneyTransferCommandSideEventHandler;

  @Override
  protected AccountCommandSideEventHandler getAccountCommandSideEventHandler() {
    return accountCommandSideEventHandler;
  }

  @Override
  protected AccountQuerySideEventHandler getAccountQuerySideEventHandler() {
    return accountQuerySideEventHandler;
  }

  @Override
  protected MoneyTransferCommandSideEventHandler getMoneyTransferCommandSideEventHandler() {
    return moneyTransferCommandSideEventHandler;
  }

  @Override
  @Test
  public void shouldStartMoneyTransfer() throws ExecutionException, InterruptedException {
    super.shouldStartMoneyTransfer();
  }

  @Override
  @Test
  public void shouldCreateAccountWithId() throws ExecutionException, InterruptedException {
    super.shouldCreateAccountWithId();
  }

  @Override
  @Test(expected = EntityNotFoundException.class)
  public void shouldFailToFindNonExistentAccount() throws Throwable {
    super.shouldFailToFindNonExistentAccount();
  }
}
