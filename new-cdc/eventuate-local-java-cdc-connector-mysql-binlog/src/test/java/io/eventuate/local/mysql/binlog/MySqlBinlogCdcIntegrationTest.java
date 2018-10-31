package io.eventuate.local.mysql.binlog;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MySqlBinlogCdcIntegrationTestConfiguration.class,
        OffsetStoreMockConfiguration.class})
public class MySqlBinlogCdcIntegrationTest extends AbstractMySqlBinlogCdcIntegrationTest {
}
