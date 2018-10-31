package io.eventuate.local.postgres.wal;

import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.db.log.test.common.AbstractDbLogBasedCdcKafkaPublisherTest;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@ActiveProfiles("PostgresWal")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PostgresWalCdcIntegrationTestConfiguration.class)
public class PostgresWalCdcKafkaPublisherTest extends AbstractDbLogBasedCdcKafkaPublisherTest {

  @Autowired
  private PostgresWalClient postgresWalClient;

  @Override
  public void init() {
    super.init();

    postgresWalClient.addBinlogEntryHandler(eventuateSchema,
            sourceTableNameSupplier.getSourceTableName(),
            new BinlogEntryToPublishedEventConverter(),
            cdcDataPublisher);

    postgresWalClient.start();
  }

  @Override
  public void clear() {
    postgresWalClient.stop();
  }
}
