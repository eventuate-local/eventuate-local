package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.common.exception.EventuateLocalPublishingException;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.test.common.OffsetStoreMock;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.util.function.Consumer;

public abstract class AbstractMySQLCdcProcessorTest extends CdcProcessorTest {

  private MySqlBinaryLogClient mySqlBinaryLogClient;

  private OffsetStore offsetStore = new OffsetStoreMock();

  private DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore = new DebeziumOffsetStoreMock();

  @Value("${spring.datasource.url}")
  private String dataSourceUrl;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private EventuateConfigurationProperties eventuateConfigurationProperties;

  @Autowired
  private CuratorFramework curatorFramework;

  @Override
  @Before
  public void init() {
    super.init();
    mySqlBinaryLogClient = new MySqlBinaryLogClient(
            eventuateConfigurationProperties.getDbUserName(),
            eventuateConfigurationProperties.getDbPassword(),
            dataSourceUrl,
            dataSource,
            eventuateConfigurationProperties.getBinlogClientId(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection(),
            curatorFramework,
            eventuateConfigurationProperties.getLeadershipLockPath(),
            offsetStore,
            debeziumBinlogOffsetKafkaStore);
  }

  @Override
  protected void prepareBinlogEntryHandler(Consumer<PublishedEvent> consumer) {
    mySqlBinaryLogClient.addBinlogEntryHandler(eventuateSchema,
            sourceTableNameSupplier.getSourceTableName(),
            new BinlogEntryToPublishedEventConverter(),
            new CdcDataPublisher<PublishedEvent>(null, null, null) {
              @Override
              public void handleEvent(PublishedEvent publishedEvent) throws EventuateLocalPublishingException {
                consumer.accept(publishedEvent);
              }
            });
  }

  @Override
  public void onEventSent(PublishedEvent publishedEvent) {
    offsetStore.save(publishedEvent.getBinlogFileOffset().get());
  }

  @Override
  protected void startEventProcessing() {
    mySqlBinaryLogClient.start();
  }

  @Override
  protected void stopEventProcessing() {
    mySqlBinaryLogClient.stop();
  }
}
