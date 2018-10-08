package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.sync.AggregateRepository;
import io.eventuate.sync.EventuateAggregateStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(JdbcAutoConfigurationIntegrationTestConfiguration.class)
public class JdbcEventMetadataAutoConfigurationIntegrationSyncTestConfiguration {

  @Bean
  public AggregateRepository<Account, AccountCommand> syncAccountRepository(EventuateAggregateStore aggregateStore) {
    return new AggregateRepository<>(Account.class, aggregateStore);
  }

  @Bean
  public AccountMetadataEventHandler accountMetadataEventHandler() {
    return new AccountMetadataEventHandler();
  }
}
