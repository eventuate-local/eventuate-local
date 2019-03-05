package io.eventuate.local.polling;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
  private int pollingRetryIntervalInMilliseconds;

  @Value("${spring.datasource.driver-class-name}")
  private String dataSourceDriverClassName;

  public PollingDao(PollingDataProvider<EVENT_BEAN, EVENT, ID> pollingDataParser,
          DataSource dataSource,
          int maxEventsPerPolling,
          int maxAttemptsForPolling,
          int pollingRetryIntervalInMilliseconds) {

    if (maxEventsPerPolling <= 0) {
      throw new IllegalArgumentException("Max events per polling parameter should be greater than 0.");
    }

    this.pollingDataParser = pollingDataParser;
    this.dataSource = dataSource;
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

  public List<EVENT> findEventsToPublish() {

    String query;
    // Use correct SQL dialect for MSSQL
    if ("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(dataSourceDriverClassName)) {
      query = String.format("SELECT TOP (:limit) * FROM %s WHERE %s = 0 ORDER BY %s ASC",
              pollingDataParser.table(), pollingDataParser.publishedField(), pollingDataParser.idField());
    } else {
      query = String.format("SELECT * FROM %s WHERE %s = 0 ORDER BY %s ASC LIMIT :limit",
              pollingDataParser.table(), pollingDataParser.publishedField(), pollingDataParser.idField());
    }

    List<EVENT_BEAN> messageBeans = handleConnectionLost(() -> namedParameterJdbcTemplate.query(query,
      ImmutableMap.of("limit", maxEventsPerPolling), new BeanPropertyRowMapper(pollingDataParser.eventBeanClass())));

    return messageBeans.stream().map(pollingDataParser::transformEventBeanToEvent).collect(Collectors.toList());
  }

  public void markEventsAsPublished(List<EVENT> events) {

    List<ID> ids = events.stream().map(message -> pollingDataParser.getId(message)).collect(Collectors.toList());

    String query = String.format("UPDATE %s SET %s = 1 WHERE %s in (:ids)",
        pollingDataParser.table(), pollingDataParser.publishedField(), pollingDataParser.idField());

    handleConnectionLost(() -> namedParameterJdbcTemplate.update(query, ImmutableMap.of("ids", ids)));
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
