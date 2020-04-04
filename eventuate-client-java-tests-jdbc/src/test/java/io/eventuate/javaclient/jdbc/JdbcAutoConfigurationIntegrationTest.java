package io.eventuate.javaclient.jdbc;

import io.eventuate.javaclient.spring.tests.common.AbstractSpringAccountIntegrationReactiveTest;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JdbcAutoConfigurationIntegrationTestConfiguration.class)
@IntegrationTest
public class JdbcAutoConfigurationIntegrationTest extends AbstractSpringAccountIntegrationReactiveTest {


}
