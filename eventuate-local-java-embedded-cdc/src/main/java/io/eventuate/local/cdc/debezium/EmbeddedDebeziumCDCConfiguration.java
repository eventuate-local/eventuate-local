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
@EnableConfigurationProperties({EmbeddedDebeziumCDCConfigurationProperties.class, EventuateKafkaConfigurationProperties.class, EventuateLocalZookeperConfigurationProperties.class})
public class EmbeddedDebeziumCDCConfiguration {

  @Bean
  public EmbeddedDebeziumCDC embeddedDebeziumCDC(@Value("${spring.datasource.url}") String dataSourceURL,
                                                 EmbeddedDebeziumCDCConfigurationProperties embeddedDebeziumCDCConfigurationProperties,
                                                 EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                 CuratorFramework  client) {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);


    return new EmbeddedDebeziumCDC(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            jdbcUrl,
            embeddedDebeziumCDCConfigurationProperties.getDbUserName(),
            embeddedDebeziumCDCConfigurationProperties.getDbPassword(), client);
  }

  @Bean(destroyMethod = "close")
  public CuratorFramework curatorFramework(EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client = CuratorFrameworkFactory.
            builder().retryPolicy(retryPolicy)
            .connectString(eventuateLocalZookeperConfigurationProperties.getConnectionString())
            .build();
    client.start();
    return client;
  }


}
