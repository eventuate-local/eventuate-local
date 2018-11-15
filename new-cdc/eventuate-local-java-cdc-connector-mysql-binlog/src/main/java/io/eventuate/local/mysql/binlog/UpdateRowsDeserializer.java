package io.eventuate.local.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.deserialization.UpdateRowsEventDataDeserializer;
import com.github.shyiko.mysql.binlog.io.ByteArrayInputStream;
import io.eventuate.local.db.log.common.DbLogMetrics;

import java.io.IOException;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Map;

public class UpdateRowsDeserializer extends UpdateRowsEventDataDeserializer {

  private DbLogMetrics dbLogMetrics;

  public UpdateRowsDeserializer(Map<Long, TableMapEventData> tableMapEventByTableId, DbLogMetrics dbLogMetrics) {
    super(tableMapEventByTableId);
    this.dbLogMetrics = dbLogMetrics;
  }

  @Override
  protected Serializable[] deserializeRow(long tableId, BitSet includedColumns, ByteArrayInputStream inputStream) throws IOException {
    dbLogMetrics.onBinlogEntryProcessed();
    return super.deserializeRow(tableId, includedColumns, inputStream);
  }
}
