package io.eventuate.local.java.spring.jdbc.jdbckafkastore;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = JdbcAutoConfigurationIntegrationWithSnapshotsTestConfiguration.class)
public class JdbcAutoConfigurationWithSnapshotsIntegrationTest extends AbstractJdbcAutoConfigurationWithSnapshotsIntegrationTest {

}
