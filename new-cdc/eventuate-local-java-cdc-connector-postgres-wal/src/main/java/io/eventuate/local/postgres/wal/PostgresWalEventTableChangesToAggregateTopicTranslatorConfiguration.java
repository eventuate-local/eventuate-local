package io.eventuate.local.postgres.wal;

import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.CommonReplicationEventTableChangesToAggregateTopicTranslatorConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableConfigurationProperties({EventuateConfigurationProperties.class})
@Import({CommonReplicationEventTableChangesToAggregateTopicTranslatorConfiguration.class})
public class PostgresWalEventTableChangesToAggregateTopicTranslatorConfiguration {

  @Bean
  @Profile("PostgresWal")
  public PostgresWalClient<PublishedEvent> postgresBinaryLogClien(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                                  PostgresWalMessageParser<PublishedEvent> postgresWalMessageParser) {
    return new PostgresWalClient<>(postgresWalMessageParser,
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection());
  }

  @Bean
  @Profile("PostgresWal")
  public PostgresWalMessageParser<PublishedEvent> postgresReplicationMessageParser() {
    return new PostgresWalJsonMessageParser();
  }
}
