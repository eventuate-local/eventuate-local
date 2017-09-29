package io.eventuate.local.cdc.debezium;

import com.google.common.collect.ImmutableMap;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class EventPollingDao {

  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public EventPollingDao(DataSource dataSource) {
	this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

  public List<EventToPublish> findEventsToPublish(int limit) {
    return namedParameterJdbcTemplate.query("SELECT * FROM events WHERE published = 0 ORDER BY event_id ASC limit :limit",
        ImmutableMap.of("limit", limit), new BeanPropertyRowMapper(EventToPublish.class));

  }

  public void markEventsAsPublished(List<String> ids) {
    namedParameterJdbcTemplate.update("UPDATE events SET published = 1 WHERE event_id in (:ids)", ImmutableMap.of("ids", ids));
  }
}
