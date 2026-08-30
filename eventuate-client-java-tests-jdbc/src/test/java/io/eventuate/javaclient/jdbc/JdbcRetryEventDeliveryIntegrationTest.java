package io.eventuate.javaclient.jdbc;

import io.eventuate.EntityWithIdAndVersion;
import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.example.banking.domain.CreateAccountCommand;
import io.eventuate.sync.AggregateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest(classes = JdbcRetryEventDeliveryIntegrationTestConfiguration.class)
public class JdbcRetryEventDeliveryIntegrationTest {


  @Autowired
  private AggregateRepository<Account, AccountCommand> accountRepository;

  @Autowired
  private JdbcRetryEventDeliveryIntegrationTestEventHandler eventHandler;

  @Test
  public void shouldCreateAccount() {
    EntityWithIdAndVersion<Account> saveResult = accountRepository.save(new CreateAccountCommand(new BigDecimal("10.23")));
    eventHandler.eventuallyContains("JdbcRetryEventDeliveryIntegrationTestEventHandler", ctx -> ctx.getEventId().equals(saveResult.getEntityVersion()));
  }
}


