package io.eventuate.local.cdc.debezium;


import io.eventuate.local.java.jdbckafkastore.EventuateLocalConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EventPollingDaoTest.EventPollingTestConfiguration.class)
@DirtiesContext
@IntegrationTest
public class EventPollingDaoTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private EventPollingDao eventPollingDao;

  @org.springframework.context.annotation.Configuration
  @Import({EventuateLocalConfiguration.class, EventTableChangesToAggregateTopicRelayConfiguration.class})
  @EnableAutoConfiguration
  public static class EventPollingTestConfiguration {
  }

  @Before
  public void before() {
    /*Or should it be moved to data either?*/

    jdbcTemplate.update("TRUNCATE events CASCADE");

    jdbcTemplate.update("INSERT INTO events VALUES ('id1', 'type1', 'data1', 'entityType1', 'entityId1', 'triggeringEvent1', 'meta1', 0)");
    jdbcTemplate.update("INSERT INTO events VALUES ('id2', 'type2', 'data2', 'entityType2', 'entityId2', 'triggeringEvent2', NULL, 0)");
    jdbcTemplate.update("INSERT INTO events VALUES ('id3', 'type3', 'data3', 'entityType3', 'entityId3', 'triggeringEvent3', 'meta3', 1)");
  }

  @Test
  public void testFindAndPublish() {
    List<EventToPublish> eventsToPublish = eventPollingDao.findEventsToPublish(1000);

    Assert.assertEquals(2, eventsToPublish.size());

    EventToPublish event1 = eventsToPublish.get(0);

    Assert.assertEquals("id1", event1.getEventId());
    Assert.assertEquals("type1", event1.getEventType());
    Assert.assertEquals("data1", event1.getEventData());
    Assert.assertEquals("entityType1", event1.getEntityType());
    Assert.assertEquals("entityId1", event1.getEntityId());
    Assert.assertEquals("triggeringEvent1", event1.getTriggeringEvent());
    Assert.assertEquals("meta1", event1.getMetadata());


    EventToPublish event2 = eventsToPublish.get(1);

    Assert.assertEquals("id2", event2.getEventId());
    Assert.assertEquals("type2", event2.getEventType());
    Assert.assertEquals("data2", event2.getEventData());
    Assert.assertEquals("entityType2", event2.getEntityType());
    Assert.assertEquals("entityId2", event2.getEntityId());
    Assert.assertEquals("triggeringEvent2", event2.getTriggeringEvent());
    Assert.assertNull(event2.getMetadata());

    eventPollingDao.markEventsAsPublished(eventsToPublish.stream().map(eventToPublish ->
        eventToPublish.getEventId()).collect(Collectors.toList()));

    Assert.assertTrue(eventPollingDao.findEventsToPublish(1000).isEmpty());
  }

    @Test
  public void testLimit() {
    List<EventToPublish> eventsToPublish = eventPollingDao.findEventsToPublish(1);

    Assert.assertEquals(1, eventsToPublish.size());
  }

}
