package io.eventuate.local.cdc.debezium;


import io.eventuate.local.java.jdbckafkastore.EventuateLocalConfiguration;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.apache.curator.framework.CuratorFramework;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TableWithDashInNameRelayTest.TableWithDashInNameRelayTestConfiguration.class)
@DirtiesContext
@IntegrationTest
public class TableWithDashInNameRelayTest extends AbstractTopicRelayTest {

  @org.springframework.context.annotation.Configuration
  @Import({EventuateLocalConfiguration.class})
  @EnableAutoConfiguration
  public static class TableWithDashInNameRelayTestConfiguration extends EventTableChangesToAggregateTopicRelayConfiguration {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EventTableChangesToAggregateTopicRelayConfigurationProperties relayConfigProps;

    @Value("${spring.datasource.url}")
    private String dataSourceURL;

    private DataSource makeDataSource() {
      JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
      DriverManagerDataSource dataSource = new DriverManagerDataSource();
      dataSource.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
      dataSource.setUrl(dataSourceURL);
      dataSource.setUsername(relayConfigProps.getDbUserName());
      dataSource.setPassword(relayConfigProps.getDbPassword());
      return dataSource;
    }

    @Override
    public EventTableChangesToAggregateTopicRelay embeddedDebeziumCDC(@Value("${spring.datasource.url}") String dataSourceURL, EventTableChangesToAggregateTopicRelayConfigurationProperties eventTableChangesToAggregateTopicRelayConfigurationProperties, EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties, CuratorFramework client, CdcStartupValidator cdcStartupValidator) {
      ResourceDatabasePopulator rdp = new ResourceDatabasePopulator(new ClassPathResource("/cdc-test-schema.sql"));
      rdp.execute(makeDataSource());

      return super.embeddedDebeziumCDC(dataSourceURL, eventTableChangesToAggregateTopicRelayConfigurationProperties, eventuateKafkaConfigurationProperties, client, cdcStartupValidator);
    }
  }

}
