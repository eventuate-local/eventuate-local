package io.eventuate.local.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import io.eventuate.local.common.SchemaAndTable;

import javax.sql.DataSource;

public class MySqlBinlogCdcMonitoringTimestampExtractor extends AbstractMySqlBinlogExtractor {

  public MySqlBinlogCdcMonitoringTimestampExtractor(DataSource dataSource) {
    super(dataSource);
  }

  public long extract(SchemaAndTable schemaAndTable, WriteRowsEventData eventData) {
    updateColumnOrders(schemaAndTable);

    return (Long) getValue(schemaAndTable, eventData, "last_time");
  }

  public long extract(SchemaAndTable schemaAndTable, UpdateRowsEventData eventData) {
    updateColumnOrders(schemaAndTable);

    return (Long) getValue(schemaAndTable, eventData, "last_time");
  }
}
