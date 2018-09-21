package io.eventuate.local.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogEntryHandler;
import io.eventuate.local.common.BinlogFileOffset;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MySqlBinlogEntryHandler extends BinlogEntryHandler {
  private MySqlBinlogEntryExtractor mySqlBinlogEntryExtractor;

  public MySqlBinlogEntryHandler(EventuateSchema eventuateSchema,
                                 MySqlBinlogEntryExtractor mySqlBinlogEntryExtractor,
                                 String sourceTableName,
                                 BiConsumer<BinlogEntry, Optional<BinlogFileOffset>> eventConsumer) {

    super(eventuateSchema, sourceTableName, eventConsumer);

    this.mySqlBinlogEntryExtractor = mySqlBinlogEntryExtractor;
  }

  public void accept(WriteRowsEventData eventData,
                     String binlogFilename,
                     long offset,
                     Optional<BinlogFileOffset> startingBinlogFileOffset) {
    try {
      MySqlBinlogEntryExtractor extractor = mySqlBinlogEntryExtractor;
      BiConsumer<BinlogEntry, Optional<BinlogFileOffset>> consumer = eventConsumer;

      BinlogEntry entry = extractor.extract(eventData, binlogFilename, offset);

      consumer.accept(entry, startingBinlogFileOffset);
    } catch (IOException e) {
      throw new RuntimeException("Event row parsing exception", e);
    }
  }
}
