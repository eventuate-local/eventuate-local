package io.eventuate.local.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.SchemaAndTable;

import javax.sql.DataSource;

public class MySqlBinlogEntryExtractor extends AbstractMySqlBinlogExtractor {


  public MySqlBinlogEntryExtractor(DataSource dataSource) {
    super(dataSource);
  }

  public BinlogEntry extract(SchemaAndTable schemaAndTable, WriteRowsEventData eventData, String binlogFilename, long position) {
    updateColumnOrders(schemaAndTable);

    return new BinlogEntry() {
      @Override
      public Object getColumn(String name) {
        return getValue(schemaAndTable, eventData, name);
      }

      @Override
      public BinlogFileOffset getBinlogFileOffset() {
        return new BinlogFileOffset(binlogFilename, position);
      }
    };
  }
}
