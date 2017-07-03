package io.eventuate.local.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import io.eventuate.local.common.BinLogEvent;

import java.io.IOException;


public interface IWriteRowsEventDataParser<M extends BinLogEvent> {
  M parseEventData(WriteRowsEventData eventData, String binlogFilename, long position) throws IOException;
}
