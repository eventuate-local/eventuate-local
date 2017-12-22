package io.eventuate.local.postgres.wal;

import java.util.List;

public interface PostgresWalMessageParser<EVENT> {
  List<EVENT> parse(String message, long lastSequenceNumber);
}
