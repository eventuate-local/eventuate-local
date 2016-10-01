package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.javaclient.commonimpl.EventIdTypeAndData;
import io.eventuate.javaclient.spring.jdbc.AbstractEventuateJdbcAggregateStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * A JDBC-based aggregate store
 */
@Transactional
public class EventuateLocalAggregateCrud extends AbstractEventuateJdbcAggregateStore {


  public EventuateLocalAggregateCrud(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  @Override
  protected void publish(String aggregateType, String aggregateId, List<EventIdTypeAndData> eventsWithIds) {
    // Do nothing

  }
}
