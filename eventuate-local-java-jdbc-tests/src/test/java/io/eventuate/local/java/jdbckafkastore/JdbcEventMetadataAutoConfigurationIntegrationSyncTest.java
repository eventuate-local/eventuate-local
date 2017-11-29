package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.EntityWithIdAndVersion;
import io.eventuate.EntityWithMetadata;
import io.eventuate.EventHandlerContext;
import io.eventuate.SaveOptions;
import io.eventuate.UpdateOptions;
import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.example.banking.domain.CreateAccountCommand;
import io.eventuate.example.banking.domain.DebitAccountCommand;
import io.eventuate.sync.AggregateRepository;
import io.eventuate.testutil.ReceivedEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JdbcEventMetadataAutoConfigurationIntegrationSyncTestConfiguration.class)
@IntegrationTest
public class JdbcEventMetadataAutoConfigurationIntegrationSyncTest {


  @Autowired
  private AggregateRepository<Account, AccountCommand> accountRepository;

  @Autowired
  private AccountMetadataEventHandler accountMetadataEventHandler;

  @Test
  public void shouldCreateAccount() {
    Map<String, String> saveMetadata = Collections.singletonMap("xy", "z");
    EntityWithIdAndVersion<Account> saveResult = accountRepository.save(new CreateAccountCommand(new BigDecimal("10.23")),
            Optional.of(new SaveOptions().withEventMetadata(saveMetadata)));

    EntityWithMetadata<Account> findResult = accountRepository.find(saveResult.getEntityId());

    assertEquals(Optional.of(saveMetadata), findResult.getEvents().get(0).getMetadata());

    Map<String, String> updateMetadata = Collections.singletonMap("abc", "d");

    EntityWithIdAndVersion<Account> updateResult = accountRepository.update(saveResult.getEntityId(), new DebitAccountCommand(new BigDecimal("1.34"), null),
            Optional.of(new UpdateOptions().withEventMetadata(updateMetadata)));

    EntityWithMetadata<Account> findResult2 = accountRepository.find(saveResult.getEntityId());

    assertEquals(Optional.of(updateMetadata), findResult2.getEvents().get(1).getMetadata());

    ReceivedEvent createEvent = accountMetadataEventHandler.eventuallyContains("accountMetadataEventHandler - save", saveResult.getEntityVersion());
    assertEquals(Optional.of(saveMetadata), createEvent.getEventMetadata());

    ReceivedEvent updateEvent = accountMetadataEventHandler.eventuallyContains("accountMetadataEventHandler - update", updateResult.getEntityVersion());
    assertEquals(Optional.of(updateMetadata), updateEvent.getEventMetadata());

  }
}


