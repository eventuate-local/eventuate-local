package io.eventuate.local.unified.cdc;

public enum CdcPipelineType {
  MYSQL_BINLOG("eventuate-local-mysql-binlog"),
  EVENT_POLLING("eventuate-local-event-polling"),
  POSTGRES_WAL("eventuate-local-postgres-wal");

  public String stringRepresentation;

  CdcPipelineType(String stringRepresentation) {
    this.stringRepresentation = stringRepresentation;
  }
}
