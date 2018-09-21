package io.eventuate.local.mysql.binlog;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntryToPublishedEventConverter;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.common.SourceTableNameSupplier;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.test.util.CdcProcessorTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.util.function.Consumer;

public abstract class AbstractMySQLCdcProcessorTest extends CdcProcessorTest {

  @Autowired
  private MySqlBinaryLogClient mySqlBinaryLogClient;

  @Autowired
  private OffsetStore offsetStore;

  @Value("${spring.datasource.url}")
  private String dataSourceUrl;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Autowired
  private SourceTableNameSupplier sourceTableNameSupplier;

  @Override
  protected CdcProcessor<PublishedEvent> createCdcProcessor() {
    return new MySQLCdcProcessor<PublishedEvent>(mySqlBinaryLogClient,
            new BinlogEntryToPublishedEventConverter(),
            sourceTableNameSupplier.getSourceTableName(),
            eventuateSchema) {
      @Override
      public void start(Consumer consumer) {
        super.start(consumer);
        mySqlBinaryLogClient.start();
      }

      @Override
      public void stop() {
        mySqlBinaryLogClient.stop();
        super.stop();
      }
    };
  }

  @Override
  protected void onEventSent(PublishedEvent publishedEvent) {
    offsetStore.save(publishedEvent.getBinlogFileOffset());
  }
}
