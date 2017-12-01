package io.eventuate.local.cdc.debezium;


import io.eventuate.local.java.jdbckafkastore.EventuateLocalConfiguration;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.testutil.CustomDBCreator;
import io.eventuate.local.testutil.CustomDBTestConfiguration;
import io.eventuate.local.testutil.SqlScriptEditor;
import org.apache.curator.framework.CuratorFramework;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PollingBasedEventTableChangesToAggregateTopicRelayCustomDBTest.EventTableChangesToAggregateTopicRelayTestConfiguration.class)
@DirtiesContext
@IntegrationTest
public class PollingBasedEventTableChangesToAggregateTopicRelayCustomDBTest extends AbstractTopicRelayTest {

  @org.springframework.context.annotation.Configuration
  @Import({CustomDBTestConfiguration.class, EventuateLocalConfiguration.class, EventTableChangesToAggregateTopicRelayConfiguration.class})
  @EnableAutoConfiguration
  public static class EventTableChangesToAggregateTopicRelayTestConfiguration {

    @Autowired
    private CustomDBCreator customDBCreator;

    @Autowired
    private SqlScriptEditor eventuateLocalCustomDBSqlEditor;

    @Bean
    @Primary
    @Profile("EventuatePolling")
    public EventTableChangesToAggregateTopicRelay pollingCDC(EventPollingDao eventPollingDao,
            EventTableChangesToAggregateTopicRelayConfigurationProperties eventTableChangesToAggregateTopicRelayConfigurationProperties,
            EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
            CuratorFramework client,
            CdcStartupValidator cdcStartupValidator) {

      customDBCreator.create(eventuateLocalCustomDBSqlEditor);

      return new PollingBasedEventTableChangesToAggregateTopicRelay(eventPollingDao,
              eventTableChangesToAggregateTopicRelayConfigurationProperties.getPollingIntervalInMilliseconds(),
              eventuateKafkaConfigurationProperties.getBootstrapServers(),
              client,
              cdcStartupValidator,
              new TakeLeadershipAttemptTracker(eventTableChangesToAggregateTopicRelayConfigurationProperties.getMaxRetries(),
                      eventTableChangesToAggregateTopicRelayConfigurationProperties.getRetryPeriodInMilliseconds()),
              eventTableChangesToAggregateTopicRelayConfigurationProperties.getLeadershipLockPath()
      );
    }
  }

}
