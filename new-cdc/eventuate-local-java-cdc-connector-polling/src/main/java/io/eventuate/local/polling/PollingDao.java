package io.eventuate.local.polling;

import com.google.common.collect.ImmutableMap;
import io.eventuate.local.common.*;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class PollingDao extends BinlogEntryReader {
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private int maxEventsPerPolling;
  private int maxAttemptsForPolling;
  private int pollingRetryIntervalInMilliseconds;
  private int pollingIntervalInMilliseconds;

  public PollingDao(DataSource dataSource,
                    int maxEventsPerPolling,
                    int maxAttemptsForPolling,
                    int pollingRetryIntervalInMilliseconds,
                    int pollingIntervalInMilliseconds,
                    CuratorFramework curatorFramework,
                    String leadershipLockPath) {

    super(curatorFramework, leadershipLockPath);

    if (maxEventsPerPolling <= 0) {
      throw new IllegalArgumentException("Max events per polling parameter should be greater than 0.");
    }

    this.pollingIntervalInMilliseconds = pollingIntervalInMilliseconds;
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    this.maxEventsPerPolling = maxEventsPerPolling;
    this.maxAttemptsForPolling = maxAttemptsForPolling;
    this.pollingRetryIntervalInMilliseconds = pollingRetryIntervalInMilliseconds;
  }

  @Override
  protected void leaderStart() {
    stopCountDownLatch = new CountDownLatch(1);
    running.set(true);

    while (running.get()) {
      binlogEntryHandlers.forEach(this::processEvents);

      try {
        Thread.sleep(pollingIntervalInMilliseconds);
      } catch (InterruptedException e) {
        logger.error(e.getMessage(), e);
        running.set(false);
      }
    }

    stopCountDownLatch.countDown();
  }

  public void processEvents(BinlogEntryHandler handler) {
    String idField = handler.getSourceTableNameSupplier().getIdField();
    String publishedField = handler.getSourceTableNameSupplier().getPublishedField();

    String findEventsQuery = String.format("SELECT * FROM %s WHERE %s = 0 ORDER BY %s ASC LIMIT :limit",
            handler.getQualifiedTable(), publishedField, idField);

    SqlRowSet sqlRowSet = handleConnectionLost(() ->
      namedParameterJdbcTemplate.queryForRowSet(findEventsQuery, ImmutableMap.of("limit", maxEventsPerPolling)));

    List<Object> ids = new ArrayList<>();

    while (sqlRowSet.next()) {
      ids.add(sqlRowSet.getObject(handler.getSourceTableNameSupplier().getIdField()));

      handler.publish(new BinlogEntry() {
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
            handler.getQualifiedTable(),publishedField, idField);

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
          running.set(false);
          stopCountDownLatch.countDown();
          throw new RuntimeException(ie);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
