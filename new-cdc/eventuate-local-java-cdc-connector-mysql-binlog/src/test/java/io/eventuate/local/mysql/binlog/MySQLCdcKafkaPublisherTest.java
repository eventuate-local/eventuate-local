package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.test.util.CdcKafkaPublisherTest;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MySqlBinlogCdcIntegrationTestConfiguration.class,
        KafkaOffsetStoreConfiguration.class})
public class MySQLCdcKafkaPublisherTest extends CdcKafkaPublisherTest {
  @Autowired
  private MySqlBinaryLogClient mySqlBinaryLogClient;

  @Before
  @Override
  public void init() {
    super.init();

    mySqlBinaryLogClient.addBinlogEntryHandler(eventuateSchema,
            sourceTableNameSupplier.getSourceTableName(),
            new BinlogEntryToPublishedEventConverter(),
            publishingStrategy);

    mySqlBinaryLogClient.start();
  }

  @After
  @Override
  public void clear() {
    mySqlBinaryLogClient.stop();
  }
}
