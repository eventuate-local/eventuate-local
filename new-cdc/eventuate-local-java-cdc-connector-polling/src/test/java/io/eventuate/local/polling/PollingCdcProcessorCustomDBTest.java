package io.eventuate.local.polling;

import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.testutil.CustomDBCreator;
import io.eventuate.local.testutil.CustomDBTestConfiguration;
import io.eventuate.local.testutil.SqlScriptEditor;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PollingCdcProcessorCustomDBTest.Config.class)
@IntegrationTest
public class PollingCdcProcessorCustomDBTest extends AbstractPollingCdcProcessorTest {

  @Configuration
  @Import({CustomDBTestConfiguration.class, PollingIntegrationTestConfiguration.class})
  public static class Config {

    @Autowired
    private CustomDBCreator customDBCreator;

    @Autowired
    private SqlScriptEditor eventuateLocalCustomDBSqlEditor;

    @Bean
    @Primary
    public CdcProcessor<PublishedEvent> pollingCdcProcessor(EventuateConfigurationProperties eventuateConfigurationProperties,
            PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao) {

      customDBCreator.create(eventuateLocalCustomDBSqlEditor);
      return new PollingCdcProcessor<>(pollingDao, eventuateConfigurationProperties.getPollingIntervalInMilliseconds());
    }
  }
}
