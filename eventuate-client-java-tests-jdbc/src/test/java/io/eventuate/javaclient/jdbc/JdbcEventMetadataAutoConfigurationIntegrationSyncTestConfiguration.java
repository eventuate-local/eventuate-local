package io.eventuate.javaclient.jdbc;

import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.sync.AggregateRepository;
import io.eventuate.sync.EventuateAggregateStoreCrud;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(JdbcAutoConfigurationIntegrationTestConfiguration.class)
public class JdbcEventMetadataAutoConfigurationIntegrationSyncTestConfiguration {

  @Bean
  public AggregateRepository<Account, AccountCommand> syncAccountRepository(EventuateAggregateStoreCrud aggregateStore) {
    return new AggregateRepository<>(Account.class, aggregateStore);
  }

  @Bean
  public AccountMetadataEventHandler accountMetadataEventHandler() {
    return new AccountMetadataEventHandler();
  }
}
