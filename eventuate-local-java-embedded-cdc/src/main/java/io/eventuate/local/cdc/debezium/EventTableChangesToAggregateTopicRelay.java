package io.eventuate.local.cdc.debezium;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.config.Configuration;
import io.debezium.embedded.EmbeddedEngine;
import io.eventuate.local.common.AggregateTopicMapping;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.KafkaOffsetBackingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * Subscribes to changes made to EVENTS table and publishes them to aggregate topics
 */
public class EventTableChangesToAggregateTopicRelay {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateKafkaProducer producer;
  private EmbeddedEngine engine;

  public static String kafkaBootstrapServers;
  private final JdbcUrl jdbcUrl;
  private final String dbUser;
  private final String dbPassword;
  private final LeaderSelector leaderSelector;

  public EventTableChangesToAggregateTopicRelay(String kafkaBootstrapServers,
                                                JdbcUrl jdbcUrl,
                                                String dbUser, String dbPassword, CuratorFramework client) {
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    this.jdbcUrl = jdbcUrl;
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;

    leaderSelector = new LeaderSelector(client, "/eventuatelocal/cdc/leader", new LeaderSelectorListener() {

      @Override
      public void takeLeadership(CuratorFramework client) throws Exception {
        takeLeadership();
      }

      private void takeLeadership() throws InterruptedException {
        logger.info("Taking leadership");
        try {
          CompletableFuture<Object> completion = startCapturingChanges();
          try {
            completion.get();
          } catch (InterruptedException e) {
            logger.error("Interrupted while taking leadership");
          }
        } catch (Throwable t) {
          logger.error("In takeLeadership", t);
          throw t instanceof RuntimeException ? (RuntimeException)t : new RuntimeException(t);
        } finally {
          logger.debug("TakeLeadership returning");
        }
      }

      @Override
      public void stateChanged(CuratorFramework client, ConnectionState newState) {

        logger.debug("StateChanged: {}", newState);

        switch (newState) {
          case SUSPENDED:
            resignLeadership();
            break;

          case RECONNECTED:
            try {
              takeLeadership();
            } catch (InterruptedException e) {
              logger.error("While handling RECONNECTED", e);
            }
            break;

          case LOST:
            resignLeadership();
            break;
        }
      }

      private void resignLeadership() {
        logger.info("Resigning leadership");
        try {
          stopCapturingChanges();
        } catch (InterruptedException e) {
          logger.error("While handling SUSPEND", e);
        }
      }
    });
  }

  @PostConstruct
  public void start() {
    logger.info("CDC initialized. Ready to become leader");
    leaderSelector.start();
  }

  public CompletableFuture<Object> startCapturingChanges() throws InterruptedException {

    logger.debug("Starting to capture changes");

    producer = new EventuateKafkaProducer(kafkaBootstrapServers);

    String connectorName = "my-sql-connector";
    Configuration config = Configuration.create()
                                    /* begin engine properties */
            .with("connector.class",
                    "io.debezium.connector.mysql.MySqlConnector")

            .with("offset.storage", KafkaOffsetBackingStore.class.getName())
            .with("bootstrap.servers", kafkaBootstrapServers)
            .with("offset.storage.topic", "eventuate.local.cdc." + connectorName + ".offset.storage")

            .with("poll.interval.ms", 50)
            .with("offset.flush.interval.ms", 6000)
                                    /* begin connector properties */
            .with("name", connectorName)
            .with("database.hostname", jdbcUrl.getHost())
            .with("database.port", jdbcUrl.getPort())
            .with("database.user", dbUser)
            .with("database.password", dbPassword)
            .with("database.server.id", 85744)
            .with("database.server.name", "my-app-connector")
            //.with("database.whitelist", "eventuate")
            .with("database.history",
                    io.debezium.relational.history.KafkaDatabaseHistory.class.getName())
            .with("database.history.kafka.topic",
                    "eventuate.local.cdc." + connectorName + ".history.kafka.topic")
            .with("database.history.kafka.bootstrap.servers",
                    kafkaBootstrapServers)
            .build();

    CompletableFuture<Object> completion = new CompletableFuture<>();
    engine = EmbeddedEngine.create()
            .using((success, message, throwable) -> {
              if (success)
                completion.complete(null);
              else
                completion.completeExceptionally(new RuntimeException("Engine failed to start" + message, throwable));
            })
            .using(config)
            .notifying(this::handleEvent)
            .build();

    Executor executor = Executors.newCachedThreadPool();
    executor.execute(() -> {
      try {
        engine.run();
      } catch (Throwable t) {
        t.printStackTrace();
      }
    });

    logger.debug("Started engine");
    return completion;
  }

  @PreDestroy
  public void stop() throws InterruptedException {
    //stopCapturingChanges();
    leaderSelector.close();
  }

  public void stopCapturingChanges() throws InterruptedException {

    logger.debug("Stopping to capture changes");

    if (producer != null)
      producer.close();


    if (engine != null) {

      logger.debug("Stopping Debezium engine");
      engine.stop();

      try {
        while (!engine.await(30, TimeUnit.SECONDS)) {
          logger.debug("Waiting another 30 seconds for the embedded engine to shut down");
        }
      } catch (InterruptedException e) {
        Thread.interrupted();
      }
    }
  }

  private void handleEvent(SourceRecord sourceRecord) {
    logger.trace("Got record");
    String topic = sourceRecord.topic();
    if ("my-app-connector.eventuate.events".equals(topic)) {
      Struct value = (Struct) sourceRecord.value();
      Struct after = value.getStruct("after");

      String eventId = after.getString("event_id");
      String eventType = after.getString("event_type");
      String eventData = after.getString("event_data");
      String entityType = after.getString("entity_type");
      String entityId = after.getString("entity_id");
      String triggeringEvent = after.getString("triggering_event");
      PublishedEvent pe = new PublishedEvent(eventId,
              entityId, entityType,
              eventData,
              eventType);


      String aggregateTopic = AggregateTopicMapping.aggregateTypeToTopic(entityType);
      String json = toJson(pe);

      if (logger.isInfoEnabled())
        logger.debug("Publishing triggeringEvent={}, event={}", triggeringEvent, json);

      try {
        producer.send(
                aggregateTopic,
                entityId,
                json
        ).get(10, TimeUnit.SECONDS);
      } catch (RuntimeException e) {
        logger.error("error publishing to " + aggregateTopic, e);
        throw e;
      } catch (Throwable e) {
        logger.error("error publishing to " + aggregateTopic, e);
        throw new RuntimeException(e);
      }
    }
  }

  public static String toJson(PublishedEvent eventInfo) {
    ObjectMapper om = new ObjectMapper();
    try {
      return om.writeValueAsString(eventInfo);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

//  public static class MyKafkaOffsetBackingStore extends KafkaOffsetBackingStore {
//
//    @Override
//    public void configure(WorkerConfig configs) {
//      Map<String, Object> updatedConfig = new HashMap<>(configs.originals());
//      updatedConfig.put("bootstrap.servers", kafkaBootstrapServers);
//      super.configure(new WorkerConfig(configs.));
//    }
//  }

}
