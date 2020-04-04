package io.eventuate.javaclient.micronaut.tests.common;

import io.eventuate.EntityNotFoundException;
import io.eventuate.example.banking.services.AccountCommandSideEventHandler;
import io.eventuate.example.banking.services.AccountQuerySideEventHandler;
import io.eventuate.example.banking.services.MoneyTransferCommandSideEventHandler;
import io.eventuate.javaclient.tests.common.AbstractAccountIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

public abstract class AbstractMicronautAccountIntegrationTest extends AbstractAccountIntegrationTest {
  @Inject
  private AccountCommandSideEventHandler accountCommandSideEventHandler;

  @Inject
  private AccountQuerySideEventHandler accountQuerySideEventHandler;

  @Inject
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
  @Test
  public void shouldFailToFindNonExistentAccount() throws Throwable {
    Assertions.assertThrows(EntityNotFoundException.class, super::shouldFailToFindNonExistentAccount);
  }
}
