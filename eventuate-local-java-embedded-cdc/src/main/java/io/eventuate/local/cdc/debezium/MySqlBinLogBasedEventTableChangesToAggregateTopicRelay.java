package io.eventuate.local.cdc.debezium;


import io.debezium.config.Configuration;
import io.debezium.embedded.EmbeddedEngine;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.KafkaOffsetBackingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MySqlBinLogBasedEventTableChangesToAggregateTopicRelay extends EventTableChangesToAggregateTopicRelay {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final JdbcUrl jdbcUrl;
  private final String dbUser;
  private final String dbPassword;

  private EmbeddedEngine engine;

  private Optional<String> eventuateDatabase;

  public MySqlBinLogBasedEventTableChangesToAggregateTopicRelay(String kafkaBootstrapServers,
                                                                JdbcUrl jdbcUrl,
                                                                String dbUser,
                                                                String dbPassword,
                                                                CuratorFramework client,
                                                                CdcStartupValidator cdcStartupValidator,
                                                                TakeLeadershipAttemptTracker takeLeadershipAttemptTracker,
                                                                String leadershipLockPath,
                                                                Optional<String> eventuateDatabase) {

    super(kafkaBootstrapServers, client, cdcStartupValidator, takeLeadershipAttemptTracker, leadershipLockPath);

    this.jdbcUrl = jdbcUrl;
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;

    this.eventuateDatabase = eventuateDatabase;
  }

  public CompletableFuture<Object> startCapturingChanges() throws InterruptedException {
    logger.debug("Starting to capture changes");

    cdcStartupValidator.validateEnvironment();

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
            // Unnecessary.with("database.whitelist", jdbcUrl.getDatabase())
            .with("table.whitelist", eventuateDatabase.orElse(jdbcUrl.getDatabase()) + ".events")
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
              if (success) {
                completion.complete(null);
              }
              else
                completion.completeExceptionally(new RuntimeException("Engine through exception" + message, throwable));
            })
            .using(config)
            .notifying(this::receiveEvent)
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

  @Override
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

  private void receiveEvent(SourceRecord sourceRecord) {
    logger.trace("Got record");
    String topic = sourceRecord.topic();
    if (String.format("my-app-connector.%s.events", eventuateDatabase.orElse(jdbcUrl.database)).equals(topic)) {
      Struct value = (Struct) sourceRecord.value();
      Struct after = value.getStruct("after");

      String eventId = after.getString("event_id");
      String eventType = after.getString("event_type");
      String eventData = after.getString("event_data");
      String entityType = after.getString("entity_type");
      String entityId = after.getString("entity_id");
      String triggeringEvent = after.getString("triggering_event");
      Optional<String> metadata = Optional.ofNullable(after.getString("metadata"));

      handleEvent(new EventToPublish(eventId, eventType, eventData, entityType, entityId, triggeringEvent, metadata));
    }
  }
}
