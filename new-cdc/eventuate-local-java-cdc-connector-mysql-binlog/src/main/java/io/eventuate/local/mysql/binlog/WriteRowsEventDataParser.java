package io.eventuate.local.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.github.shyiko.mysql.binlog.event.deserialization.json.JsonBinary;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.EventuateConstants;
import io.eventuate.local.common.PublishedEvent;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WriteRowsEventDataParser implements IWriteRowsEventDataParser<PublishedEvent> {

  private DataSource dataSource;

  private final String sourceTableName;

  private static final String EVENT_ID_FIELDNAME = "event_id";
  private static final String EVENT_TYPE_FIELDNAME = "event_type";
  private static final String EVENT_DATA_FIELDNAME = "event_data";
  private static final String ENTITY_ID_FIELDNAME = "entity_id";
  private static final String ENTITY_TYPE_FIELDNAME = "entity_type";
  private static final String EVENT_METADATA_FIELDNAME = "metadata";

  private Map<String, Integer> columnOrders = new HashMap<>();

  private String database;

  public WriteRowsEventDataParser(DataSource dataSource, String sourceTableName, String database) {
    this.dataSource = dataSource;
    this.sourceTableName = sourceTableName;
    this.database = database;
  }

  @Override
  public PublishedEvent parseEventData(WriteRowsEventData eventData, String binlogFilename, long position) throws IOException {
    if (columnOrders.isEmpty()) {
      try {
        getColumnOrders();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    String eventDataValue;
    if(getValue(eventData, EVENT_DATA_FIELDNAME) instanceof String) {
      eventDataValue = (String) getValue(eventData, EVENT_DATA_FIELDNAME);
    } else {
      eventDataValue = JsonBinary.parseAsString((byte[])getValue(eventData, EVENT_DATA_FIELDNAME));
    }
    return new PublishedEvent(
            (String)getValue(eventData, EVENT_ID_FIELDNAME),
            (String)getValue(eventData, ENTITY_ID_FIELDNAME),
            (String)getValue(eventData, ENTITY_TYPE_FIELDNAME),
            eventDataValue,
            (String)getValue(eventData, EVENT_TYPE_FIELDNAME),
            new BinlogFileOffset(binlogFilename, position),
            Optional.ofNullable((String)getValue(eventData, EVENT_METADATA_FIELDNAME))
    );
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
                   metaData.getColumns(EventuateConstants.EMPTY_DATABASE_SCHEMA.equals(database) ? null : database, "public", sourceTableName.toLowerCase(), null)) {

        while (columnResultSet.next()) {
          columnOrders.put(columnResultSet.getString("COLUMN_NAME").toLowerCase(),
                  columnResultSet.getInt("ORDINAL_POSITION"));
        }
      }
    }
  }
}
