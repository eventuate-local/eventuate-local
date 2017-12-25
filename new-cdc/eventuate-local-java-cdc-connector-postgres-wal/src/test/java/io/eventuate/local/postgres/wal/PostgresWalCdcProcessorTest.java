package io.eventuate.local.postgres.wal;

import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("PostgresWal")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PostgresWalCdcIntegrationTestConfiguration.class)
@IntegrationTest
public class PostgresWalCdcProcessorTest extends AbstractPostgresWalCdcProcessorTest {
}
