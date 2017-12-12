package io.eventuate.local.cdc.debezium;


import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalConfiguration;
import io.eventuate.local.testutil.EmptyDBTestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MySqlBinLogBasedEventTableChangesToAggregateTopicRelayEmptyEventuateDBTest.EventTableChangesToAggregateTopicRelayTestConfiguration.class)
@DirtiesContext
public class MySqlBinLogBasedEventTableChangesToAggregateTopicRelayEmptyEventuateDBTest extends AbstractTopicRelayTest {

  @org.springframework.context.annotation.Configuration
  @Import({EmptyDBTestConfiguration.class, EventuateLocalConfiguration.class, EventTableChangesToAggregateTopicRelayConfiguration.class})
  @EnableAutoConfiguration
  public static class EventTableChangesToAggregateTopicRelayTestConfiguration {
  }

  @Value("${eventuate.database.schema}")
  private String eventuateDatabaseSchema;

  @Test
  public void testProperty() {
    Assert.assertEquals(EventuateSchema.EMPTY_SCHEMA, eventuateDatabaseSchema);
  }
}
