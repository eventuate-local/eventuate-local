package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.db.log.common.OffsetKafkaStore;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

public class DebeziumBinlogOffsetKafkaStore extends OffsetKafkaStore {

  public DebeziumBinlogOffsetKafkaStore(String dbHistoryTopicName, EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {
    super(dbHistoryTopicName, eventuateKafkaConfigurationProperties);
  }

  @Override
  protected BinlogFileOffset handleRecord(ConsumerRecord<String, String> record) {
    Map<String, Object> map = JSonMapper.fromJson(record.value(), Map.class);
    Object pos = map.get("pos");
    return new BinlogFileOffset((String)map.get("file"), pos instanceof Long ? ((Long) pos) : ((Integer) pos));
  }

  @Override
  public void save(BinlogFileOffset binlogFileOffset) {
  }

  @Override
  public void stop() {
  }
}
