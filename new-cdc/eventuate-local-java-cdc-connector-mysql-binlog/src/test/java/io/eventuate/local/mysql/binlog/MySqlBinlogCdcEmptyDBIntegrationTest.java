package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.EventuateConstants;
import io.eventuate.local.testutil.EmptyDBTestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {EmptyDBTestConfiguration.class, MySqlBinlogCdcIntegrationTestConfiguration.class})
@IntegrationTest
public class MySqlBinlogCdcEmptyDBIntegrationTest extends AbstractMySqlBinlogCdcIntegrationTest {

  @Value("${eventuate.database.schema}")
  private String eventuateDatabaseSchema;

  @Test
  public void testProperty() {
    Assert.assertEquals(EventuateConstants.EMPTY_DATABASE_SCHEMA, eventuateDatabaseSchema);
  }
}
