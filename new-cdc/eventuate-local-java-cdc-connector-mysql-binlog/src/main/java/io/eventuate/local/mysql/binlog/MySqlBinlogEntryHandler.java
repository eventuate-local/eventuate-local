package io.eventuate.local.mysql.binlog;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;

import java.io.IOException;
import java.util.Optional;

public class MySqlBinlogEntryHandler<EVENT extends BinLogEvent> extends BinlogEntryHandler<EVENT> {
  private boolean couldReadDuplicateEntries = true;
  private MySqlBinlogEntryExtractor mySqlBinlogEntryExtractor;

  public MySqlBinlogEntryHandler(EventuateSchema eventuateSchema,
                                 String sourceTableName,
                                 MySqlBinlogEntryExtractor mySqlBinlogEntryExtractor,
                                 BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                                 CdcDataPublisher<EVENT> dataPublisher) {

    super(eventuateSchema, sourceTableName, binlogEntryToEventConverter, dataPublisher);

    this.mySqlBinlogEntryExtractor = mySqlBinlogEntryExtractor;
  }


  public void accept(WriteRowsEventData eventData,
                     String binlogFilename,
                     long offset,
                     Optional<BinlogFileOffset> startingBinlogFileOffset) {
    try {
      BinlogEntry entry = mySqlBinlogEntryExtractor.extract(eventData, binlogFilename, offset);

      if (couldReadDuplicateEntries) {
        if (startingBinlogFileOffset.map(s -> s.isSameOrAfter(entry.getBinlogFileOffset())).orElse(false)) {
          return;
        } else {
          couldReadDuplicateEntries = false;
        }
      }

      cdcDataPublisher.handleEvent(binlogEntryToEventConverter.convert(entry));
    } catch (IOException e) {
      throw new RuntimeException("Event row parsing exception", e);
    }
  }
}
