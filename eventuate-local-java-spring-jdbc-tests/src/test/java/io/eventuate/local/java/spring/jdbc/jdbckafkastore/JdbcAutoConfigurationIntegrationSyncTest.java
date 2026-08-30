package io.eventuate.local.java.spring.jdbc.jdbckafkastore;

import io.eventuate.javaclient.spring.tests.common.AbstractSpringAccountIntegrationSyncTest;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = JdbcAutoConfigurationIntegrationTestConfiguration.class)
public class JdbcAutoConfigurationIntegrationSyncTest extends AbstractSpringAccountIntegrationSyncTest {


}
