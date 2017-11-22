package io.eventuate.local.cdc.debezium;


import io.eventuate.local.java.jdbckafkastore.EventuateLocalConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PollingBasedEventTableChangesToAggregateTopicRelayCustomDBTest.EventTableChangesToAggregateTopicRelayTestConfiguration.class)
@DirtiesContext
@IntegrationTest
public class PollingBasedEventTableChangesToAggregateTopicRelayCustomDBTest extends AbstractTopicRelayTest {

  @org.springframework.context.annotation.Configuration
  @Import({EventuateLocalConfiguration.class, EventTableChangesToAggregateTopicRelayConfiguration.class})
  @EnableAutoConfiguration
  @PropertySource({"/customdb.properties"})
  public static class EventTableChangesToAggregateTopicRelayTestConfiguration {
  }

  @Autowired
  private DataSource dataSource;

  @Before
  public void createDefaultDB() {
    Resource resource = new ClassPathResource("custom-db-mysql-schema.sql");
    ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
    databasePopulator.execute(dataSource);
  }
}
