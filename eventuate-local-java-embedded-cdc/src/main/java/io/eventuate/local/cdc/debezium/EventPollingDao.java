package io.eventuate.local.cdc.debezium;

import com.google.common.collect.ImmutableMap;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.MetaDataAccessException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

public class EventPollingDao {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private DataSource dataSource;
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private int maxEventsPerPolling;
  private int maxAttemptsForPolling;
  private int delayPerPollingAttemptInMilliseconds;

  public EventPollingDao(DataSource dataSource,
    int maxEventsPerPolling,
    int maxAttemptsForPolling,
    int delayPerPollingAttemptInMilliseconds
  ) {
	this.dataSource = dataSource;
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
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
          dataSource.getConnection().isValid(1);
        } catch (InterruptedException | SQLException ie) {
          logger.error(ie.getMessage(), ie);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
