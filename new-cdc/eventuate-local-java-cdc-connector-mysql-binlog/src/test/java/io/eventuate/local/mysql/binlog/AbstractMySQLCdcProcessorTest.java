package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractMySQLCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  private MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient;

  @Autowired
  private DatabaseOffsetKafkaStore binlogOffsetKafkaStore;

  @Autowired
  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore;

  @Override
  protected CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore, debeziumBinlogOffsetKafkaStore);
  }

  @Override
  protected void onEventSent(PublishedEvent publishedEvent) {
    binlogOffsetKafkaStore.save(publishedEvent.getBinlogFileOffset());
  }
}
