package io.eventuate.local.cdc.debezium;

import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({EventTableChangesToAggregateTopicRelayConfigurationProperties.class, EventuateKafkaConfigurationProperties.class, EventuateLocalZookeperConfigurationProperties.class})
public class EventTableChangesToAggregateTopicRelayConfiguration {

  @Bean
  public EventTableChangesToAggregateTopicRelay embeddedDebeziumCDC(@Value("${spring.datasource.url}") String dataSourceURL,
                                                                    EventTableChangesToAggregateTopicRelayConfigurationProperties eventTableChangesToAggregateTopicRelayConfigurationProperties,
                                                                    EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                    CuratorFramework  client) {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);


    return new EventTableChangesToAggregateTopicRelay(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            jdbcUrl,
            eventTableChangesToAggregateTopicRelayConfigurationProperties.getDbUserName(),
            eventTableChangesToAggregateTopicRelayConfigurationProperties.getDbPassword(), client);
  }

  @Bean(destroyMethod = "close")
  public CuratorFramework curatorFramework(EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties) {
    String connectionString = eventuateLocalZookeperConfigurationProperties.getConnectionString();
    return makeStartedCuratorClient(connectionString);
  }

  static CuratorFramework makeStartedCuratorClient(String connectionString) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client = CuratorFrameworkFactory.
            builder().retryPolicy(retryPolicy)
            .connectString(connectionString)
            .build();
    client.start();
    return client;
  }


}
