package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MySqlBinlogCdcIntegrationTestConfiguration.class)
@IntegrationTest
public class MySQLCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  private MySqlBinaryLogClient<PublishedEvent> mySqlBinaryLogClient;

  @Autowired
  private DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore;


  @Override
  protected CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore);
  }

  @Override
  protected void onEventSent(PublishedEvent publishedEvent) {
    binlogOffsetKafkaStore.save(publishedEvent.getBinlogFileOffset());
  }
}
