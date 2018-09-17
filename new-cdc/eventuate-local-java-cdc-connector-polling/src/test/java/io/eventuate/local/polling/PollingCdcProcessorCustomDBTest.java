package io.eventuate.local.polling;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.testutil.CustomDBCreator;
import io.eventuate.local.testutil.CustomDBTestConfiguration;
import io.eventuate.local.testutil.SqlScriptEditor;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.*;
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

    @Bean
    @Primary
    public CdcProcessor<PublishedEvent> pollingCdcProcessor(@Value("${spring.datasource.url}") String dbUrl,
                                                            EventuateConfigurationProperties eventuateConfigurationProperties,
                                                            PollingDao pollingDao,
                                                            PollingDataProvider pollingDataProvider,
                                                            EventuateSchema eventuateSchema,
                                                            SourceTableNameSupplier sourceTableNameSupplier) {

      return new PollingCdcProcessor<>(pollingDao,
              eventuateConfigurationProperties.getPollingIntervalInMilliseconds(),
              pollingDataProvider,
              new BinlogEntryToPublishedEventConverter(),
              dbUrl,
              eventuateSchema,
              sourceTableNameSupplier.getSourceTableName());
    }
  }
}
