package io.eventuate.local.cdc.debezium;


import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Monitors changes made to EVENTS table and publishes them to aggregate topics
 */
public class EventTableChangesToAggregateTopicPollingRelay extends AbstractEventTableChangesToAggregateTopicRelay {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private int requestPeriodInMilliseconds;
  private boolean watcherRunning = false;

  public EventTableChangesToAggregateTopicPollingRelay(
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
            List<String> handledEventIds = new ArrayList<>();

            namedParameterJdbcTemplate.query("SELECT * FROM EVENTS WHERE SENT = 0 ORDER BY EVENT_ID ASC", rs -> {
              logger.trace("Got row");

              String eventId = rs.getString("event_id");
              String eventType = rs.getString("event_type");
              String eventData = rs.getString("event_data");
              String entityType = rs.getString("entity_type");
              String entityId = rs.getString("entity_id");
              String triggeringEvent = rs.getString("triggering_event");
              Optional<String> metadata = Optional.ofNullable(rs.getString("metadata"));

              handleEvent(eventId, eventType, eventData, entityType, entityId, triggeringEvent, metadata);

              handledEventIds.add(eventId);
            });

            if (!handledEventIds.isEmpty()) {
              namedParameterJdbcTemplate.update("UPDATE EVENTS SET SENT = 1 WHERE EVENT_ID in (:ids)",
                      Collections.singletonMap("ids", handledEventIds));
            }

            completableFuture.complete(null);

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
