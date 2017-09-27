package io.eventuate.local.cdc.debezium;


import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Monitors changes made to EVENTS table and publishes them to aggregate topics
 */
public class PollingBasedEventTableChangesToAggregateTopicRelay extends EventTableChangesToAggregateTopicRelay {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private int requestPeriodInMilliseconds;
  private boolean watcherRunning = false;

  public PollingBasedEventTableChangesToAggregateTopicRelay(
    DataSource dataSource,
    int requestPeriodInMilliseconds,
    String kafkaBootstrapServers,
    CuratorFramework client,
    CdcStartupValidator cdcStartupValidator,
    TakeLeadershipAttemptTracker takeLeadershipAttemptTracker) {

    super(kafkaBootstrapServers, client, cdcStartupValidator, takeLeadershipAttemptTracker);
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    this.requestPeriodInMilliseconds = requestPeriodInMilliseconds;
  }

  public CompletableFuture<Object> startCapturingChanges() throws InterruptedException {
    logger.debug("Starting to capture changes");

    cdcStartupValidator.validateEnvironment();

    watcherRunning = true;
    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    producer = new EventuateKafkaProducer(kafkaBootstrapServers);

    new Thread() {
      @Override
      public void run() {

        while (watcherRunning) {
          try {

            class Event {
              String eventId;
              String eventType;
              String eventData;
              String entityType;
              String entityId;
              String triggeringEvent;
              Optional<String> metadata;
            }

            List<Event> events = new ArrayList<>();

            namedParameterJdbcTemplate.query("SELECT * FROM events WHERE published = 0 ORDER BY event_id ASC", rs -> {
              logger.trace("Got row");

              events.add(new Event() {{
                eventId = rs.getString("event_id");
                eventType = rs.getString("event_type");
                eventData = rs.getString("event_data");
                entityType = rs.getString("entity_type");
                entityId = rs.getString("entity_id");
                triggeringEvent = rs.getString("triggering_event");
                metadata = Optional.ofNullable(rs.getString("metadata"));
              }});
            });

            events.forEach(event -> handleEvent(event.eventId,
                event.eventType,
                event.eventData,
                event.entityType,
                event.entityId,
                event.triggeringEvent,
                event.metadata));

            if (!events.isEmpty()) {
              namedParameterJdbcTemplate.update("UPDATE events SET published = 1 WHERE event_id in (:ids)",
                      Collections.singletonMap("ids", events.stream().map(event -> event.eventId).collect(Collectors.toList())));
              completableFuture.complete(null);
            }

            try {
              Thread.sleep(requestPeriodInMilliseconds);
            } catch (Exception e) {
              logger.error(e.getMessage(), e);
            }
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            completableFuture.completeExceptionally(new RuntimeException("Polling exception" + e.getMessage(), e));
          }
        }
      }
    }.start();

    return completableFuture;
  }

  @Override
  public void stopCapturingChanges() throws InterruptedException {

    logger.debug("Stopping to capture changes");

    if (producer != null)
      producer.close();

    watcherRunning = false;
  }

}
