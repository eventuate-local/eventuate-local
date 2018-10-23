package io.eventuate.local.polling;

import com.google.common.collect.ImmutableMap;
import io.eventuate.local.common.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class PollingDao extends BinlogEntryReader {
  private static final String PUBLISHED_FIELD = "published";

  private DataSource dataSource;
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private int maxEventsPerPolling;
  private int maxAttemptsForPolling;
  private int pollingRetryIntervalInMilliseconds;
  private int pollingIntervalInMilliseconds;
  private Map<SchemaAndTable, String> pkFields = new HashMap<>();

  public PollingDao(MeterRegistry meterRegistry,
                    String dataSourceUrl,
                    DataSource dataSource,
                    int maxEventsPerPolling,
                    int maxAttemptsForPolling,
                    int pollingRetryIntervalInMilliseconds,
                    int pollingIntervalInMilliseconds,
                    CuratorFramework curatorFramework,
                    String leadershipLockPath,
                    long uniqueId) {

    super(meterRegistry, curatorFramework, leadershipLockPath, dataSourceUrl, dataSource, uniqueId);

    if (maxEventsPerPolling <= 0) {
      throw new IllegalArgumentException("Max events per polling parameter should be greater than 0.");
    }

    this.dataSource = dataSource;
    this.pollingIntervalInMilliseconds = pollingIntervalInMilliseconds;
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    this.maxEventsPerPolling = maxEventsPerPolling;
    this.maxAttemptsForPolling = maxAttemptsForPolling;
    this.pollingRetryIntervalInMilliseconds = pollingRetryIntervalInMilliseconds;
  }

  @Override
  protected void leaderStart() {
    super.leaderStart();

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

    String pk = getPrimaryKey(handler);

    String findEventsQuery = String.format("SELECT * FROM %s WHERE %s = 0 ORDER BY %s ASC LIMIT :limit",
            handler.getQualifiedTable(), PUBLISHED_FIELD, pk);

    SqlRowSet sqlRowSet = handleConnectionLost(() ->
      namedParameterJdbcTemplate.queryForRowSet(findEventsQuery, ImmutableMap.of("limit", maxEventsPerPolling)));

    List<Object> ids = new ArrayList<>();

    while (sqlRowSet.next()) {
      onEventReceived();

      ids.add(sqlRowSet.getObject(pk));

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
            handler.getQualifiedTable(), PUBLISHED_FIELD, pk);

    if (!ids.isEmpty()) {
      handleConnectionLost(() -> namedParameterJdbcTemplate.update(markEventsAsReadQuery, ImmutableMap.of("ids", ids)));
    }
  }

  private String getPrimaryKey(BinlogEntryHandler handler) {
    SchemaAndTable schemaAndTable = handler.getSchemaAndTable();

    if (pkFields.containsKey(schemaAndTable)) {
      return pkFields.get(schemaAndTable);
    }

    String pk = handleConnectionLost(() -> queryPrimaryKey(handler));

    pkFields.put(schemaAndTable, pk);

    return pk;
  }

  private String queryPrimaryKey(BinlogEntryHandler handler) throws SQLException {
    String pk;
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      ResultSet resultSet = connection
              .getMetaData()
              .getPrimaryKeys(null,
                      handler.getSchemaAndTable().getSchema(),
                      handler.getSchemaAndTable().getTableName());

      if (resultSet.next()) {
        pk = resultSet.getString("COLUMN_NAME");
        if (resultSet.next()) {
          throw new RuntimeException("Table %s has more than one primary key");
        }
      } else {
        throw new RuntimeException("Cannot get table: result set is empty");
      }
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (SQLException e) {
        logger.warn(e.getMessage(), e);
      }
    }

    return pk;
  }

  private <T> T handleConnectionLost(Callable<T> query) {
    int attempt = 0;

    while(true) {
      try {
        T result = query.call();
        if (attempt > 0)
          logger.info("Reconnected to database");
        return result;
      } catch (DataAccessResourceFailureException | PSQLException e) {

        logger.error(String.format("Could not access database %s - retrying in %s milliseconds", e.getMessage(), pollingRetryIntervalInMilliseconds), e);

        if (attempt++ >= maxAttemptsForPolling) {
          throw new RuntimeException(e);
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
