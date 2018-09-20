package io.eventuate.local.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogEntryHandler;

import java.io.IOException;
import java.util.function.Consumer;

public class MySqlBinlogEntryHandler extends BinlogEntryHandler {
  private MySqlBinlogEntryExtractor mySqlBinlogEntryExtractor;

  public MySqlBinlogEntryHandler(EventuateSchema eventuateSchema,
                                 MySqlBinlogEntryExtractor mySqlBinlogEntryExtractor,
                                 String sourceTableName,
                                 Consumer<BinlogEntry> eventConsumer) {

    super(eventuateSchema, sourceTableName, eventConsumer);

    this.mySqlBinlogEntryExtractor = mySqlBinlogEntryExtractor;
  }

  public void accept(WriteRowsEventData eventData, String binlogFilename, long offset) {
    try {
      MySqlBinlogEntryExtractor extractor = mySqlBinlogEntryExtractor;
      Consumer<BinlogEntry> consumer = eventConsumer;

      BinlogEntry entry = extractor.extract(eventData, binlogFilename, offset);

      consumer.accept(entry);
    } catch (IOException e) {
      throw new RuntimeException("Event row parsing exception", e);
    }
  }
}
