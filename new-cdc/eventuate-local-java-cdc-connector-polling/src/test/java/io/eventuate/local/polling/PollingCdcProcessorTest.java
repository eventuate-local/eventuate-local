package io.eventuate.local.polling;

import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("EventuatePolling")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PollingIntegrationTestConfiguration.class)
@IntegrationTest
public class PollingCdcProcessorTest extends AbstractPollingCdcProcessorTest {
}
