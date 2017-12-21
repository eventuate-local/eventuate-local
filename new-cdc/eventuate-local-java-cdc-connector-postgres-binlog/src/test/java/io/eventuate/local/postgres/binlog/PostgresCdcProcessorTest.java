package io.eventuate.local.postgres.binlog;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("PostgresBinLog")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PostgresBinlogCdcIntegrationTestConfiguration.class)
public class PostgresCdcProcessorTest extends AbstractPostgresCdcProcessorTest {
}
