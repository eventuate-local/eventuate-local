package io.eventuate.local.polling;

import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.testutil.CustomDBCreator;
import io.eventuate.local.testutil.CustomDBTestConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {PollingCdcProcessorCustomDBTest.Config.class,
        CustomDBTestConfiguration.class, PollingIntegrationTestConfiguration.class})
@IntegrationTest
public class PollingCdcProcessorCustomDBTest extends AbstractPollingCdcProcessorTest {

  public static class Config {

    @Autowired
    private CustomDBCreator customDBCreator;

    @Bean
    @Primary
    @Profile("EventuatePolling")
    public CdcProcessor<PublishedEvent> pollingCdcProcessor(EventuateConfigurationProperties eventuateConfigurationProperties,
            PollingDao<PublishedEventBean, PublishedEvent, String> pollingDao) {

      customDBCreator.create();
      return new PollingCdcProcessor<>(pollingDao, eventuateConfigurationProperties.getPollingIntervalInMilliseconds());
    }
  }
}
