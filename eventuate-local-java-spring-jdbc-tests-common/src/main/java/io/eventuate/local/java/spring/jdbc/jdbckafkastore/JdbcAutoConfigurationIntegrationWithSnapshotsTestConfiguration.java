package io.eventuate.local.java.spring.jdbc.jdbckafkastore;

import io.eventuate.example.banking.domain.Account;
import io.eventuate.example.banking.domain.AccountCommand;
import io.eventuate.example.banking.domain.AccountSnapshotStrategy;
import io.eventuate.sync.AggregateRepository;
import io.eventuate.sync.EventuateAggregateStoreCrud;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.spy;

@Configuration
@EnableAutoConfiguration
public class JdbcAutoConfigurationIntegrationWithSnapshotsTestConfiguration {

  @Bean
  public AccountSnapshotStrategy accountSnapshotStrategy() {
    return spy(AccountSnapshotStrategy.class);
  }

  @Bean
  public AggregateRepository<Account, AccountCommand> accountRepositorySync(EventuateAggregateStoreCrud aggregateStore) {
    return new AggregateRepository<>(Account.class, aggregateStore);
  }
}
