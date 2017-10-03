package io.eventuate.local.cdc.debezium;

import com.google.common.collect.ImmutableMap;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.MetaDataAccessException;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

public class EventPollingDao {
  private Logger logger = LoggerFactory.getLogger(getClass());

  DataSourceFactory dataSourceFactory;
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private int maxEventsPerPolling;
  private int maxAttemptsForPolling;
  private int delayPerPollingAttemptInMilliseconds;

  public EventPollingDao(DataSourceFactory dataSourceFactory,
    int maxEventsPerPolling,
    int maxAttemptsForPolling,
    int delayPerPollingAttemptInMilliseconds
  ) {
    this.dataSourceFactory = dataSourceFactory;
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSourceFactory.createDataSource());
    this.maxEventsPerPolling = maxEventsPerPolling;
    this.maxAttemptsForPolling = maxAttemptsForPolling;
    this.delayPerPollingAttemptInMilliseconds = delayPerPollingAttemptInMilliseconds;
  }

  public int getMaxEventsPerPolling() {
    return maxEventsPerPolling;
  }

  public void setMaxEventsPerPolling(int maxEventsPerPolling) {
    this.maxEventsPerPolling = maxEventsPerPolling;
  }

  public List<EventToPublish> findEventsToPublish() {
    return handleConnectionLost(() -> namedParameterJdbcTemplate.query("SELECT * FROM events WHERE published = 0 ORDER BY event_id ASC limit :limit",
            ImmutableMap.of("limit", maxEventsPerPolling), new BeanPropertyRowMapper(EventToPublish.class)));
  }

  public void markEventsAsPublished(List<String> ids) {
    handleConnectionLost(() -> namedParameterJdbcTemplate.update("UPDATE events SET published = 1 WHERE event_id in (:ids)",
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
          Thread.sleep(delayPerPollingAttemptInMilliseconds);
          namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSourceFactory.createDataSource());
        } catch (InterruptedException ie) {
          logger.error(ie.getMessage(), ie);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
