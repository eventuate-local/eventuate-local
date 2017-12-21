package io.eventuate.local.postgres.binlog;

import io.eventuate.local.testutil.CustomDBCreator;
import io.eventuate.local.testutil.CustomDBTestConfiguration;
import io.eventuate.local.testutil.SqlScriptEditor;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CustomDBTestConfiguration.class, PostgresBinlogCdcIntegrationTestConfiguration.class})
public class PostgresBinlogCdcCustomDBIntegrationTest extends AbstractPostgresBinlogCdcIntegrationTest {

  @Autowired
  private CustomDBCreator customDBCreator;

  @Before
  public void createCustomDB() {
    customDBCreator.create(sqlList -> {
      sqlList.set(0, sqlList.get(0).replace("CREATE SCHEMA", "CREATE SCHEMA IF NOT EXISTS"));
      for (int i = 0; i < sqlList.size(); i++) sqlList.set(i, sqlList.get(i).replace("eventuate", "custom"));
      return sqlList;
    });
  }
}
