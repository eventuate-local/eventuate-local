package io.eventuate.local.polling;

import com.google.common.collect.ImmutableMap;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class PollingDao extends BinlogEntryReader<PollingEntryHandler<?>> {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private int maxEventsPerPolling;
  private int maxAttemptsForPolling;
  private int pollingRetryIntervalInMilliseconds;
  private CountDownLatch stopCountDownLatch = new CountDownLatch(1);
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

  public <EVENT extends BinLogEvent> void addPollingEntryHandler(EventuateSchema eventuateSchema,
                                                                 String sourceTableName,
                                                                 PollingDataProvider pollingDataProvider,
                                                                 BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                                                                 CdcDataPublisher<EVENT> dataPublisher) {

    binlogEntryHandlers.add(new PollingEntryHandler<>(eventuateSchema,
            sourceTableName,
            pollingDataProvider.publishedField(),
            pollingDataProvider.idField(),
            binlogEntryToEventConverter,
            dataPublisher));
  }

  @Override
  protected void leaderStart() {
    running.set(true);

    new Thread(() -> {

      while (running.get()) {
        try {

          binlogEntryHandlers.forEach(this::processEvents);

          try {
            Thread.sleep(pollingIntervalInMilliseconds);
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
          }
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        }
      }
      stopCountDownLatch.countDown();
    }).start();
  }

  @Override
  protected void leaderStop() {
    if (!running.compareAndSet(true, false)) {
      return;
    }

    try {
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
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
