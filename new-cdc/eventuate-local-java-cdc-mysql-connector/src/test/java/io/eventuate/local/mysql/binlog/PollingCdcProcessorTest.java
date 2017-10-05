package io.eventuate.local.mysql.binlog;

import io.eventuate.Int128;
import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.jdbckafkastore.EventuateLocalAggregateCrud;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MySqlBinlogCdcIntegrationTestConfiguration.class)
@IntegrationTest
public class PollingCdcProcessorTest extends AbstractCdcTest {

  EventuateJdbcAccess eventuateJdbcAccess;

  EventuateLocalAggregateCrud localAggregateCrud;

  private PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao;

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Before
  public void init() {
    Assume.assumeTrue(Arrays.asList(environment.getActiveProfiles()).contains("EventuatePolling"));

    eventuateJdbcAccess = applicationContext.getAutowireCapableBeanFactory().getBean(EventuateJdbcAccess.class);
    pollingDao = applicationContext.getAutowireCapableBeanFactory().getBean(PollingDao.class);
    localAggregateCrud = new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Test
  public void shouldReadNewEventsOnly() throws InterruptedException {
    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();
    PollingCdcProcessor<PublishedEventBean, PublishedEvent, String> pollingCdcProcessor = new PollingCdcProcessor<>(pollingDao, 4000);
    pollingCdcProcessor.start(publishedEvent -> {
      publishedEvents.add(publishedEvent);
    });

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);
    waitForEvent(publishedEvents, entityIdVersionAndEventIds.getEntityVersion(), LocalDateTime.now().plusSeconds(10), accountCreatedEventData);
    pollingCdcProcessor.stop();

    Thread.sleep(10000);

    publishedEvents.clear();
    pollingCdcProcessor.start(publishedEvent -> {
      publishedEvents.add(publishedEvent);
    });
    List<String> excludedIds = entityIdVersionAndEventIds.getEventIds().stream().map(Int128::asString).collect(Collectors.toList());

    accountCreatedEventData = generateAccountCreatedEvent();
    entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);
    waitForEventExcluding(publishedEvents, entityIdVersionAndEventIds.getEntityVersion(), LocalDateTime.now().plusSeconds(10), accountCreatedEventData, excludedIds);
    pollingCdcProcessor.stop();
  }

  @Test
  public void shouldReadUnprocessedEventsAfterStartup() throws InterruptedException {
    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(localAggregateCrud, accountCreatedEventData);

    PollingCdcProcessor<PublishedEventBean, PublishedEvent, String> pollingCdcProcessor = new PollingCdcProcessor<>(pollingDao, 4000);
    pollingCdcProcessor.start(publishedEvents::add);

    waitForEvent(publishedEvents, entityIdVersionAndEventIds.getEntityVersion(), LocalDateTime.now().plusSeconds(20), accountCreatedEventData);
    pollingCdcProcessor.stop();
  }

  private PublishedEvent waitForEventExcluding(BlockingQueue<PublishedEvent> publishedEvents, Int128 eventId, LocalDateTime deadline, String eventData, List<String> excludedIds) throws InterruptedException {
    PublishedEvent result = null;
    while (LocalDateTime.now().isBefore(deadline)) {
      long millis = ChronoUnit.MILLIS.between(deadline, LocalDateTime.now());
      PublishedEvent event = publishedEvents.poll(millis, TimeUnit.MILLISECONDS);
      if (event != null) {
        if (event.getId().equals(eventId.asString()) && eventData.equals(event.getEventData()))
          result = event;
        if (excludedIds.contains(event.getId()))
          throw new RuntimeException("Wrong event found in the queue");
      }
    }
    if (result != null)
      return result;
    throw new RuntimeException("event not found: " + eventId);
  }
}
