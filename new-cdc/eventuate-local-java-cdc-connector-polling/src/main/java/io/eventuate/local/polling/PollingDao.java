package io.eventuate.local.polling;

import com.google.common.collect.ImmutableMap;
import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogFileOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PollingDao {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private int maxEventsPerPolling;
  private int maxAttemptsForPolling;
  private int pollingRetryIntervalInMilliseconds;

  public PollingDao(DataSource dataSource,
          int maxEventsPerPolling,
          int maxAttemptsForPolling,
          int pollingRetryIntervalInMilliseconds) {

    if (maxEventsPerPolling <= 0) {
      throw new IllegalArgumentException("Max events per polling parameter should be greater than 0.");
    }

    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    this.maxEventsPerPolling = maxEventsPerPolling;
    this.maxAttemptsForPolling = maxAttemptsForPolling;
    this.pollingRetryIntervalInMilliseconds = pollingRetryIntervalInMilliseconds;
  }

  public int getMaxEventsPerPolling() {
    return maxEventsPerPolling;
  }

  public void setMaxEventsPerPolling(int maxEventsPerPolling) {
    this.maxEventsPerPolling = maxEventsPerPolling;
  }

  public void processEvents(PollingEntryHandler handler) {
    String findEventsQuery = String.format("SELECT * FROM %s WHERE %s = 0 ORDER BY %s ASC LIMIT :limit",
            handler.getQualifiedTable(), handler.getPublishedField(), handler.getIdField());

    SqlRowSet sqlRowSet = handleConnectionLost(() ->
      namedParameterJdbcTemplate.queryForRowSet(findEventsQuery, ImmutableMap.of("limit", maxEventsPerPolling)));

    List<Object> ids = new ArrayList<>();

    while (sqlRowSet.next()) {
      ids.add(sqlRowSet.getObject(handler.getIdField()));

      handler.accept(new BinlogEntry() {
        @Override
        public Object getColumn(String name) {
          return sqlRowSet.getObject(name);
        }

        @Override
        public BinlogFileOffset getBinlogFileOffset() {
          return null;
        }
      });
    }

    String markEventsAsReadQuery = String.format("UPDATE %s SET %s = 1 WHERE %s in (:ids)",
            handler.getQualifiedTable(), handler.getPublishedField(), handler.getIdField());

    if (!ids.isEmpty()) {
      handleConnectionLost(() -> namedParameterJdbcTemplate.update(markEventsAsReadQuery, ImmutableMap.of("ids", ids)));
    }
  }

  private <T> T handleConnectionLost(Callable<T> query) {
    int attempt = 0;

    while(true) {
      try {
        T result = query.call();
        if (attempt > 0)
          logger.info("Reconnected to database");
        return result;
      } catch (DataAccessResourceFailureException e) {

        logger.error(String.format("Could not access database %s - retrying in %s milliseconds", e.getMessage(), pollingRetryIntervalInMilliseconds), e);

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
