package io.eventuate.local.polling;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogEntryToEventConverter;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.JdbcUrlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PollingCdcProcessor<EVENT> implements CdcProcessor<EVENT> {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private PollingDao pollingDao;
  private int pollingIntervalInMilliseconds;
  private PollingDataProvider pollingDataProvider;
  private BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter;
  private String dataSourceUrl;
  private EventuateSchema eventuateSchema;
  private String sourceTableName;
  private AtomicBoolean watcherRunning = new AtomicBoolean(false);
  private CountDownLatch stopCountDownLatch = new CountDownLatch(1);

  public PollingCdcProcessor(PollingDao pollingDao,
                             int pollingIntervalInMilliseconds,
                             PollingDataProvider pollingDataProvider,
                             BinlogEntryToEventConverter<EVENT> binlogEntryToEventConverter,
                             String dataSourceUrl,
                             EventuateSchema eventuateSchema,
                             String sourceTableName) {
    this.pollingDao = pollingDao;
    this.pollingIntervalInMilliseconds = pollingIntervalInMilliseconds;
    this.pollingDataProvider = pollingDataProvider;
    this.binlogEntryToEventConverter = binlogEntryToEventConverter;
    this.dataSourceUrl = dataSourceUrl;
    this.eventuateSchema = eventuateSchema;
    this.sourceTableName = sourceTableName;
  }

  public void start(Consumer<EVENT> eventConsumer) {
    if (!watcherRunning.compareAndSet(false, true)) {
      return;
    }

    PollingEntryHandler pollingEntryHandler = new PollingEntryHandler(JdbcUrlParser.parse(dataSourceUrl).getDatabase(),
            eventuateSchema,
            sourceTableName,
            binlogEntry -> eventConsumer.accept(binlogEntryToEventConverter.convert(binlogEntry)),
            pollingDataProvider.publishedField(),
            pollingDataProvider.idField());

    new Thread(() -> {

      while (watcherRunning.get()) {
        try {

          pollingDao.processEvents(pollingEntryHandler);

          try {
            Thread.sleep(pollingIntervalInMilliseconds);
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
          }
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        }
      }
      stopCountDownLatch.countDown();
    }).start();
  }

  public void stop() {
    if (!watcherRunning.compareAndSet(true, false)) {
      return;
    }
    try {
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
