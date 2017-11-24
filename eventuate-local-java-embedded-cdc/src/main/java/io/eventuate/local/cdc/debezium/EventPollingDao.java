package io.eventuate.local.cdc.debezium;

import com.google.common.collect.ImmutableMap;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.Callable;

public class EventPollingDao {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private DataSource dataSource;
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private int maxEventsPerPolling;
  private int maxAttemptsForPolling;
  private int pollingRetryIntervalInMilliseconds;
  private String eventTable;

  public EventPollingDao(DataSource dataSource,
        int maxEventsPerPolling,
        int maxAttemptsForPolling,
        int pollingRetryIntervalInMilliseconds,
        EventuateSchema eventuateSchema) {

    if (maxEventsPerPolling <= 0) {
      throw new IllegalArgumentException("Max events per polling parameter should be greater than 0.");
    }

    this.dataSource = dataSource;
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    this.maxEventsPerPolling = maxEventsPerPolling;
    this.maxAttemptsForPolling = maxAttemptsForPolling;
    this.pollingRetryIntervalInMilliseconds = pollingRetryIntervalInMilliseconds;

    eventTable = eventuateSchema.qualifyTable("events");
  }

  public int getMaxEventsPerPolling() {
    return maxEventsPerPolling;
  }

  public void setMaxEventsPerPolling(int maxEventsPerPolling) {
    this.maxEventsPerPolling = maxEventsPerPolling;
  }

  public List<EventToPublish> findEventsToPublish() {
    return handleConnectionLost(() ->
            namedParameterJdbcTemplate.query(String.format("SELECT * FROM %s WHERE published = 0 ORDER BY event_id ASC limit :limit", eventTable),
            ImmutableMap.of("limit", maxEventsPerPolling), new BeanPropertyRowMapper(EventToPublish.class)));
  }

  public void markEventsAsPublished(List<String> ids) {
    handleConnectionLost(() -> namedParameterJdbcTemplate.update(String.format("UPDATE %s SET published = 1 WHERE event_id in (:ids)", eventTable),
        ImmutableMap.of("ids", ids)));
  }

  private <T> T handleConnectionLost(Callable<T> query) {
    int attempt = 0;

    while(true) {
      try {
        return query.call();
      } catch (DataAccessResourceFailureException e) {

        logger.error(e.getMessage(), e);

        if (attempt++ >= maxAttemptsForPolling) {
          throw e;
        }

        try {
          Thread.sleep(pollingRetryIntervalInMilliseconds);
        } catch (InterruptedException ie) {
          logger.error(ie.getMessage(), ie);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
