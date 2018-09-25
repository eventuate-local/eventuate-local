package io.eventuate.local.polling;

import io.eventuate.local.testutil.CustomDBCreator;
import io.eventuate.local.testutil.CustomDBTestConfiguration;
import io.eventuate.local.testutil.SqlScriptEditor;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PollingCdcProcessorCustomDBTest.Config.class)
public class PollingCdcProcessorCustomDBTest extends AbstractPollingCdcProcessorTest {

  @Configuration
  @Import({CustomDBTestConfiguration.class, PollingIntegrationTestConfiguration.class})
  public static class Config {

    @Autowired
    private CustomDBCreator customDBCreator;

    @Autowired
    private SqlScriptEditor eventuateLocalCustomDBSqlEditor;
  }
}
