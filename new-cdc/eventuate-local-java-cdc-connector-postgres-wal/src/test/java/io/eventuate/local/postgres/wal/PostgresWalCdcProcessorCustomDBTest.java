package io.eventuate.local.postgres.wal;


import io.eventuate.local.testutil.CustomDBCreator;
import io.eventuate.local.testutil.CustomDBTestConfiguration;
import io.eventuate.local.testutil.SqlScriptEditor;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("PostgresWal")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CustomDBTestConfiguration.class, PostgresWalCdcIntegrationTestConfiguration.class})
public class PostgresWalCdcProcessorCustomDBTest extends AbstractPostgresWalCdcProcessorTest {

  @Autowired
  private CustomDBCreator customDBCreator;

  @Autowired
  private SqlScriptEditor sqlScriptEditor;

  @Before
  public void createCustomDB() {
    customDBCreator.create(sqlScriptEditor);
  }
}
