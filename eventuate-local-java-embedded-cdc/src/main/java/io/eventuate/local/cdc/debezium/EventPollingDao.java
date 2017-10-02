package io.eventuate.local.cdc.debezium;

import com.google.common.collect.ImmutableMap;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    int attempt = 0;

    while(true) {
      try {
        return namedParameterJdbcTemplate.query("SELECT * FROM events WHERE published = 0 ORDER BY event_id ASC limit :limit",
            ImmutableMap.of("limit", maxEventsPerPolling), new BeanPropertyRowMapper(EventToPublish.class));
      } catch (Exception e) {

        logger.error(e.getMessage(), e);

        if (!(e instanceof PSQLException) && !(e.getCause() instanceof PSQLException)) {
          throw e;
        }

        if (attempt++ >= maxAttemptsForPolling) {
          throw e;
        }

        try {
          Thread.sleep(delayPerPollingAttemptInMilliseconds);
          dataSource.getConnection().isValid(1);
        } catch (InterruptedException | SQLException ie) {
          logger.error(ie.getMessage(), ie);
        }
      }
    }
  }

  public void markEventsAsPublished(List<String> ids) {
    namedParameterJdbcTemplate.update("UPDATE events SET published = 1 WHERE event_id in (:ids)", ImmutableMap.of("ids", ids));
  }
}
