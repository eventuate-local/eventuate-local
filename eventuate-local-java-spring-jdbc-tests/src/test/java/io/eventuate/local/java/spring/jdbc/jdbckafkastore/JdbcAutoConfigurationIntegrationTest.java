package io.eventuate.local.java.spring.jdbc.jdbckafkastore;

import io.eventuate.javaclient.spring.tests.common.AbstractSpringAccountIntegrationReactiveTest;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = JdbcAutoConfigurationIntegrationTestConfiguration.class)
public class JdbcAutoConfigurationIntegrationTest extends AbstractSpringAccountIntegrationReactiveTest {


}
