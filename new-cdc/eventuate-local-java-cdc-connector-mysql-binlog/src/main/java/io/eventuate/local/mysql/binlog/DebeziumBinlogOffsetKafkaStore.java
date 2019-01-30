package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.db.log.common.OffsetKafkaStore;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class DebeziumBinlogOffsetKafkaStore extends OffsetKafkaStore {

  private Logger logger = LoggerFactory.getLogger(getClass());

  public DebeziumBinlogOffsetKafkaStore(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                        EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    super("eventuate.local.cdc.my-sql-connector.offset.storage",
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }

  @Override
  protected BinlogFileOffset handleRecord(ConsumerRecord<String, String> record) {
    try {
      Map<String, Object> keyMap = JSonMapper.fromJson(record.key(), Map.class);

      List<Object> payload = (List<Object>)keyMap.get("payload");
      String connector = (String)payload.get(0);
      String server = ((Map<String, String>)payload.get(1)).get("server");

      if (!"my-sql-connector".equals(connector) || !"my-app-connector".equals(server)) {
        return null;
      }

      Map<String, Object> valueMap = JSonMapper.fromJson(record.value(), Map.class);

      String file = (String)valueMap.get("file");
      long position = ((Number)valueMap.get("pos")).longValue();
      int rowsToSkip = valueMap.containsKey("row") ? ((Number)valueMap.get("row")).intValue() : 0;

      return new BinlogFileOffset(file, position, rowsToSkip);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public void save(BinlogFileOffset binlogFileOffset) {
  }

  @Override
  public void stop() {
  }
}
