package io.eventuate.local.db.log.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DbLogBasedCdcProcessor<EVENT extends BinLogEvent> implements CdcProcessor<EVENT> {

  protected DbLogClient dbLogClient;
  protected BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;
  protected String sourceTableName;
  protected EventuateSchema eventuateSchema;

  public DbLogBasedCdcProcessor(DbLogClient dbLogClient,
                                BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                                String sourceTableName,
                                EventuateSchema eventuateSchema) {

    this.dbLogClient = dbLogClient;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
    this.sourceTableName = sourceTableName;
    this.eventuateSchema = eventuateSchema;
  }

  @Override
  public void start(Consumer<EVENT> eventConsumer) {
    try {
      dbLogClient.addBinlogEntryHandler(eventuateSchema, sourceTableName, createBinlogConsumer(eventConsumer));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected BiConsumer<BinlogEntry, Optional<BinlogFileOffset>> createBinlogConsumer(Consumer<EVENT> eventConsumer) {
    return new BiConsumer<BinlogEntry, Optional<BinlogFileOffset>>() {
      private boolean couldReadDuplicateEntries = true;

      @Override
      public void accept(BinlogEntry binlogEntry, Optional<BinlogFileOffset> startingBinlogFileOffset) {
        if (couldReadDuplicateEntries) {
          if (startingBinlogFileOffset.map(s -> s.isSameOrAfter(binlogEntry.getBinlogFileOffset())).orElse(false)) {
            return;
          } else {
            couldReadDuplicateEntries = false;
          }
        }
        eventConsumer.accept(binlogEntryToEventConverter.convert(binlogEntry));
      }
    };
  }

  @Override
  public void stop() {
  }
}
