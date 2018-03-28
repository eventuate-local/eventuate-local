package io.eventuate.local.mysql.binlog;

import io.eventuate.local.db.log.test.util.AbstractDuplicatePublishingDetectorTest;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MySqlBinlogCdcIntegrationTestConfiguration.class)
public class DuplicatePublishingDetectorTest extends AbstractDuplicatePublishingDetectorTest {
}
