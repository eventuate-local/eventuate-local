package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.testutil.EmptyDBTestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = {EmptyDBTestConfiguration.class, MySqlBinlogCdcIntegrationTestConfiguration.class})
public class MySQLCdcProcessorEmptyDBTest/* extends AbstractMySQLCdcProcessorTest*/ {

  @Value("${eventuate.database.schema}")
  private String eventuateDatabaseSchema;

//  @Test
//  public void testProperty() {
//    Assert.assertEquals(EventuateSchema.EMPTY_SCHEMA, eventuateDatabaseSchema);
//  }
}
