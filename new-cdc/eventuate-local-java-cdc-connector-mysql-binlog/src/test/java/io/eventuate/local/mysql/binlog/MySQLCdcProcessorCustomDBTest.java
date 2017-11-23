package io.eventuate.local.mysql.binlog;

import io.eventuate.local.testutil.CustomDBCreator;
import io.eventuate.local.testutil.CustomDBTestConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {CustomDBTestConfiguration.class, MySqlBinlogCdcIntegrationTestConfiguration.class})
@IntegrationTest
public class MySQLCdcProcessorCustomDBTest extends AbstractMySQLCdcProcessorTest {

  @Autowired
  private CustomDBCreator customDBCreator;

  @Before
  public void createCustomDB() {
    customDBCreator.create();
  }
}
