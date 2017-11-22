package io.eventuate.local.polling;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {PollingCdcProcessorCustomDBTest.Configuration.class, PollingIntegrationTestConfiguration.class})
@IntegrationTest
public class PollingCdcProcessorCustomDBTest extends AbstractPollingCdcProcessorTest {

  @PropertySource({"/customdb.properties"})
  @org.springframework.context.annotation.Configuration
  @EnableAutoConfiguration
  public static class Configuration {
  }

  @Autowired
  private DataSource dataSource;

  @Before
  public void createDefaultDB() {
    Resource resource = new ClassPathResource("custom-db-mysql-schema.sql");
    ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
    databasePopulator.execute(dataSource);
  }
}
