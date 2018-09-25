package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.SourceTableNameSupplier;
import io.eventuate.local.db.log.test.util.AbstractDbLogBasedCdcKafkaPublisherTest;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MySqlBinlogCdcIntegrationTestConfiguration.class)
public class MySQLCdcKafkaPublisherTest extends AbstractDbLogBasedCdcKafkaPublisherTest {
  @Autowired
  private MySqlBinaryLogClient mySqlBinaryLogClient;

  @Before
  @Override
  public void init() {
    super.init();

    mySqlBinaryLogClient.addBinlogEntryHandler(eventuateSchema,
            sourceTableNameSupplier,
            new BinlogEntryToPublishedEventConverter(),
            cdcDataPublisher);

    mySqlBinaryLogClient.start();
  }

  @After
  @Override
  public void clear() {
    mySqlBinaryLogClient.stop();
  }
}
