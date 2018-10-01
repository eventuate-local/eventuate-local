package io.eventuate.local.postgres.wal;

import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogFileOffset;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PostgresWalBinlogEntryExtractor {

  public List<BinlogEntry> extract(List<PostgresWalChange> postgresWalChanges, BinlogFileOffset offset) {

    return postgresWalChanges
            .stream()
            .map(insertedEvent -> {
              List<String> columns = Arrays.asList(insertedEvent.getColumnnames());
              List<String> values = Arrays.asList(insertedEvent.getColumnvalues());

              return new BinlogEntry() {
                @Override
                public Object getColumn(String name) {
                  return values.get(columns.indexOf(name));
                }

                @Override
                public BinlogFileOffset getBinlogFileOffset() {
                  return offset;
                }
              };
            })
            .collect(Collectors.toList());
  }
}
