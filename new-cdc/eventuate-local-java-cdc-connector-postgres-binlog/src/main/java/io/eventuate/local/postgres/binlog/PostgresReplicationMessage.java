package io.eventuate.local.postgres.binlog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostgresReplicationMessage {
    private PostgresReplicationChange[] change;

    public PostgresReplicationMessage() {
    }

    public PostgresReplicationMessage(PostgresReplicationChange[] change) {
        this.change = change;
    }

    public PostgresReplicationChange[] getChange() {
        return change;
    }

    public void setChange(PostgresReplicationChange[] change) {
        this.change = change;
    }
}