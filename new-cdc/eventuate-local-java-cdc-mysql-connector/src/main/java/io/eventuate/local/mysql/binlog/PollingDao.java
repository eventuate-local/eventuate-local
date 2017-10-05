package io.eventuate.local.mysql.binlog;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class PollingDao<EVENT_BEAN, EVENT, ID> {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private PollingDataProvider<EVENT_BEAN, EVENT, ID> pollingDataParser;
  private DataSource dataSource;
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private int maxEventsPerPolling;
  private int maxAttemptsForPolling;
  private int delayPerPollingAttemptInMilliseconds;

  public PollingDao(PollingDataProvider<EVENT_BEAN, EVENT, ID> pollingDataParser,
    DataSource dataSource,
    int maxEventsPerPolling,
    int maxAttemptsForPolling,
    int delayPerPollingAttemptInMilliseconds) {

    this.pollingDataParser = pollingDataParser;
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

  public List<EVENT> findEventsToPublish() {

    String query = "SELECT * FROM " + pollingDataParser.table() +
        " WHERE " + pollingDataParser.publishedField() + " = 0 " +
        "ORDER BY " + pollingDataParser.idField() + " ASC limit :limit";

    List<EVENT_BEAN> messageBeans = handleConnectionLost(() -> namedParameterJdbcTemplate.query(query,
      ImmutableMap.of("limit", maxEventsPerPolling), new BeanPropertyRowMapper(pollingDataParser.eventBeanClass())));

    return messageBeans.stream().map(pollingDataParser::transformEventBeanToEvent).collect(Collectors.toList());
  }

  public void markEventsAsPublished(List<EVENT> events) {

    List<ID> ids = events.stream().map(message -> pollingDataParser.getId(message)).collect(Collectors.toList());

    String query = "UPDATE " + pollingDataParser.table() +
        " SET " + pollingDataParser.publishedField() + " = 1 " +
        "WHERE " + pollingDataParser.idField() + " in (:ids)";

    handleConnectionLost(() -> namedParameterJdbcTemplate.update(query, ImmutableMap.of("ids", ids)));
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
        } catch (InterruptedException ie) {
          logger.error(ie.getMessage(), ie);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
