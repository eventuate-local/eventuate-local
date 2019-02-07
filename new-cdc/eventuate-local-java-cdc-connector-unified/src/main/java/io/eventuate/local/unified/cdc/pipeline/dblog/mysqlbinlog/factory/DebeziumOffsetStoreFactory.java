package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory;

import io.eventuate.local.mysql.binlog.DebeziumBinlogOffsetKafkaStore;

public interface DebeziumOffsetStoreFactory {
  DebeziumBinlogOffsetKafkaStore create();
}
