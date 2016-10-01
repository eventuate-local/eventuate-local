package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.javaclient.spring.tests.common.AbstractAccountIntegrationSyncTest;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JdbcAutoConfigurationIntegrationTestConfiguration.class)
@IntegrationTest
public class JdbcAutoConfigurationIntegrationSyncTest extends AbstractAccountIntegrationSyncTest {


}
