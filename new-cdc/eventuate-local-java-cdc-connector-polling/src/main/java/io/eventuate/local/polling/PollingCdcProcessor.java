package io.eventuate.local.polling;

import io.eventuate.local.common.CdcProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PollingCdcProcessor<EVENT_BEAN, EVENT, ID> implements CdcProcessor<EVENT> {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private PollingDao pollingDao;
  private int pollingIntervalInMilliseconds;
  private AtomicBoolean watcherRunning = new AtomicBoolean(false);

  public PollingCdcProcessor(PollingDao<EVENT_BEAN, EVENT, ID> pollingDao, int pollingIntervalInMilliseconds) {
    this.pollingDao = pollingDao;
    this.pollingIntervalInMilliseconds = pollingIntervalInMilliseconds;
  }

  public void start(Consumer<EVENT> eventConsumer) {
    watcherRunning.set(true);

    new Thread() {
      @Override
      public void run() {

        while (watcherRunning.get()) {
          try {

            List<EVENT> eventsToPublish = pollingDao.findEventsToPublish();

            eventsToPublish.forEach(eventConsumer::accept);

            if (!eventsToPublish.isEmpty()) {

              pollingDao.markEventsAsPublished(eventsToPublish);
            }

            try {
              Thread.sleep(pollingIntervalInMilliseconds);
            } catch (Exception e) {
              logger.error(e.getMessage(), e);
            }
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
          }
        }
      }
    }.start();
  }

  public void stop() {
    watcherRunning.set(false);
  }
}
