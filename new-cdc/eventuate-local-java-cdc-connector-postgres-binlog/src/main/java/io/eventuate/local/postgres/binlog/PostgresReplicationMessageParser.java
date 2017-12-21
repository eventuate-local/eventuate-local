package io.eventuate.local.postgres.binlog;

import java.util.List;

public interface PostgresReplicationMessageParser<EVENT> {
  List<EVENT> parse(String message, long lastSequenceNumber);
}
