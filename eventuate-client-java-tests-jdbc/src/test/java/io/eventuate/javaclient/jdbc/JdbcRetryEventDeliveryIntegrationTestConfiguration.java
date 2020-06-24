package io.eventuate.javaclient.jdbc;

import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.javaclient.eventhandling.exceptionhandling.EventDeliveryExceptionHandler;
import io.eventuate.javaclient.eventhandling.exceptionhandling.EventuateClientScheduler;
import io.eventuate.javaclient.eventhandling.exceptionhandling.RetryEventDeliveryExceptionHandler;
import io.eventuate.sync.AggregateRepository;
import io.eventuate.sync.EventuateAggregateStoreCrud;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Duration;

@Configuration
@Import(JdbcAutoConfigurationIntegrationTestConfiguration.class)
public class JdbcRetryEventDeliveryIntegrationTestConfiguration {

  @Bean
  public AggregateRepository<Account, AccountCommand> syncAccountRepository(EventuateAggregateStoreCrud aggregateStore) {
    return new AggregateRepository<>(Account.class, aggregateStore);
  }

  @Bean
  public JdbcRetryEventDeliveryIntegrationTestEventHandler accountMetadataEventHandler() {
    return new JdbcRetryEventDeliveryIntegrationTestEventHandler();
  }

  @Bean
  public EventDeliveryExceptionHandler forEventHandlerRetryEventHandler(EventuateClientScheduler scheduler) {
    return new RetryEventDeliveryExceptionHandler(scheduler)
            .withExceptions(JdbcRetryEventDeliveryIntegrationTestException.class)
            .withMaxRetries(2)
            .withRetryInterval(Duration.ofSeconds(2))
            ;

  }

}
