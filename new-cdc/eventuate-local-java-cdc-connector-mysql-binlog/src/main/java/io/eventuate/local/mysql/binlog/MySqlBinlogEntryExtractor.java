package io.eventuate.local.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogFileOffset;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MySqlBinlogEntryExtractor {

  private DataSource dataSource;

  private final String sourceTableName;

  private Map<String, Integer> columnOrders = new HashMap<>();

  private EventuateSchema eventuateSchema;

  public MySqlBinlogEntryExtractor(DataSource dataSource, String sourceTableName, EventuateSchema eventuateSchema) {
    this.dataSource = dataSource;
    this.sourceTableName = sourceTableName;
    this.eventuateSchema = eventuateSchema;
  }

  public BinlogEntry extract(WriteRowsEventData eventData, String binlogFilename, long position) throws IOException {
    if (columnOrders.isEmpty()) {
      try {
        getColumnOrders();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    return new BinlogEntry() {
      @Override
      public Object getColumn(String name) {
        return getValue(eventData, name);
      }

      @Override
      public BinlogFileOffset getBinlogFileOffset() {
        return new BinlogFileOffset(binlogFilename, position);
      }
    };
  }

  private Serializable getValue(WriteRowsEventData eventData, String columnName) {
    if(columnOrders.containsKey(columnName)) {
      return eventData.getRows().get(0)[columnOrders.get(columnName) - 1];
    }
    throw new RuntimeException("Column with name [" + columnName + "] not found");
  }

  private void getColumnOrders() throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();

      try (ResultSet columnResultSet =
                   metaData.getColumns(eventuateSchema.isEmpty() ? null : eventuateSchema.getEventuateDatabaseSchema(), "public", sourceTableName.toLowerCase(), null)) {

        while (columnResultSet.next()) {
          columnOrders.put(columnResultSet.getString("COLUMN_NAME").toLowerCase(),
                  columnResultSet.getInt("ORDINAL_POSITION"));
        }
      }
    }
  }
}
