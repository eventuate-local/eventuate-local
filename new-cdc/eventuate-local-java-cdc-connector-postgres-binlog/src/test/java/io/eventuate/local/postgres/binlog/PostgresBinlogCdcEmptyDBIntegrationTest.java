package io.eventuate.local.postgres.binlog;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.testutil.EmptyDBTestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {EmptyDBTestConfiguration.class, PostgresBinlogCdcIntegrationTestConfiguration.class})
@IntegrationTest
public class PostgresBinlogCdcEmptyDBIntegrationTest extends AbstractPostgresBinlogCdcIntegrationTest {

  @Value("${eventuate.database.schema}")
  private String eventuateDatabaseSchema;

  @Test
  public void testProperty() {
    Assert.assertEquals(EventuateSchema.EMPTY_SCHEMA, eventuateDatabaseSchema);
  }
}
