package io.eventuate.local.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.SchemaAndTable;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MySqlBinlogEntryExtractor {

  private DataSource dataSource;

  private Map<SchemaAndTable, Map<String, Integer>> columnOrders = new HashMap<>();

  public MySqlBinlogEntryExtractor(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public BinlogEntry extract(String sourceTableName, String database, WriteRowsEventData eventData, String binlogFilename, long position) {
    if (!columnOrders.containsKey(new SchemaAndTable(database, sourceTableName))) {
      try {
        getColumnOrders(sourceTableName, database);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    return new BinlogEntry() {
      @Override
      public Object getColumn(String name) {
        return getValue(sourceTableName, database, eventData, name);
      }

      @Override
      public BinlogFileOffset getBinlogFileOffset() {
        return new BinlogFileOffset(binlogFilename, position);
      }
    };
  }

  private Serializable getValue(String sourceTableName, String database, WriteRowsEventData eventData, String columnName) {
    SchemaAndTable schemaAndTable = new SchemaAndTable(database, sourceTableName);

    if (columnOrders.containsKey(schemaAndTable)) {
      Map<String, Integer> order = columnOrders.get(schemaAndTable);

      if(order.containsKey(columnName)) {
        return eventData.getRows().get(0)[order.get(columnName) - 1];
      }
    }

    throw new RuntimeException("Column with name [" + columnName + "] not found");
  }

  private void getColumnOrders(String sourceTableName, String database) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();

      try (ResultSet columnResultSet = metaData.getColumns(null, database, sourceTableName.toLowerCase(), null)) {

        Map<String, Integer> order = new HashMap<>();

        while (columnResultSet.next()) {

          order.put(columnResultSet.getString("COLUMN_NAME").toLowerCase(),
                  columnResultSet.getInt("ORDINAL_POSITION"));
        }

        columnOrders.put(new SchemaAndTable(database, sourceTableName), order);
      }
    }
  }

}
