package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.javaclient.spring.jdbc.AbstractJdbcAggregateCrud;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import org.springframework.transaction.annotation.Transactional;

/**
 * A JDBC-based aggregate store
 */
@Transactional
public class EventuateLocalAggregateCrud extends AbstractJdbcAggregateCrud {


  public EventuateLocalAggregateCrud(EventuateJdbcAccess eventuateJdbcAccess) {
    super(eventuateJdbcAccess);
  }

}
