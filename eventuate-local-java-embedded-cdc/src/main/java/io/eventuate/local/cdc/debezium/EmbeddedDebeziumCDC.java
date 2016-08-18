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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EmbeddedDebeziumCDC {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateKafkaProducer producer;
  private EmbeddedEngine engine;

  public static String kafkaBootstrapServers;
  private final JdbcUrl jdbcUrl;
  private final String dbUser;
  private final String dbPassword;
  private final LeaderSelector leaderSelector;
  private CountDownLatch counterLatch;

  public EmbeddedDebeziumCDC(String kafkaBootstrapServers,
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
        startCapturingChanges();
        counterLatch = new CountDownLatch(1);
        try {
          counterLatch.await();
        } catch (InterruptedException e) {
          logger.error("Interrupted while taking leadership");
        }
      }

      @Override
      public void stateChanged(CuratorFramework client, ConnectionState newState) {
        switch (newState) {
          case SUSPENDED:
            resignLeadership();

          case RECONNECTED:
            try {
              takeLeadership();
            } catch (InterruptedException e) {
              logger.error("While handling RECONNECTED", e);
            }

          case LOST:
            resignLeadership();
        }
      }

      private void resignLeadership() {
        logger.info("Resigning leadership");
        try {
          stopCapturingChanges();
        } catch (InterruptedException e) {
          logger.error("While handling SUSPEND", e);
        }
        if (counterLatch != null && counterLatch.getCount() > 0)
          counterLatch.countDown();
      }
    });
  }

  @PostConstruct
  public void start() {
    leaderSelector.start();
  }

  public void startCapturingChanges() {

    logger.info("Starting to capture changes");

    producer = new EventuateKafkaProducer(kafkaBootstrapServers);

    String connectorName = "my-sql-connector";
    Configuration config = Configuration.create()
                                    /* begin engine properties */
            .with("connector.class",
                    "io.debezium.connector.mysql.MySqlConnector")

            .with("offset.storage", MyKafkaOffsetBackingStore.class.getName())
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
            .with("database.whitelist", "eventuate")
            .with("database.history",
                    io.debezium.relational.history.KafkaDatabaseHistory.class.getName())
            .with("database.history.kafka.topic",
                    "eventuate.local.cdc." + connectorName + ".history.kafka.topic")
            .with("database.history.kafka.bootstrap.servers",
                    kafkaBootstrapServers)
            .build();

    engine = EmbeddedEngine.create()
            .using(config)
            .using(config)
            .notifying(this::handleEvent)
            .build();

    Executor executor = Executors.newCachedThreadPool();
    executor.execute(engine);
  }

  @PreDestroy
  public void stop() throws InterruptedException {
    //stopCapturingChanges();
    leaderSelector.close();
  }

  public void stopCapturingChanges() throws InterruptedException {

    logger.info("Stopping to capture changes");

    if (producer != null)
      producer.close();


    if (engine != null) {

      logger.info("Stopping Debezium engine");
      engine.stop();

      try {
        while (!engine.await(30, TimeUnit.SECONDS)) {
          logger.info("Waiting another 30 seconds for the embedded engine to shut down");
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

      producer.send(
              AggregateTopicMapping.aggregateTypeToTopic(entityType),
              entityId,
              toJson(pe)
      );
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

  public static class MyKafkaOffsetBackingStore extends KafkaOffsetBackingStore {

    @Override
    public void configure(Map<String, ?> configs) {
      Map<String, Object> updatedConfig = new HashMap<>(configs);
      updatedConfig.put("bootstrap.servers", kafkaBootstrapServers);
      super.configure(updatedConfig);
    }
  }

}
