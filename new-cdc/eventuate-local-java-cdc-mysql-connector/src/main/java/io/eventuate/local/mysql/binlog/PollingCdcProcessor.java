package io.eventuate.local.mysql.binlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class PollingCdcProcessor<EVENT_BEAN, EVENT, ID> {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private PollingDao pollingDao;
  private int requestPeriodInMilliseconds;
  private boolean watcherRunning = false;

  public PollingCdcProcessor(PollingDao<EVENT_BEAN, EVENT, ID> pollingDao, int requestPeriodInMilliseconds) {
    this.pollingDao = pollingDao;
    this.requestPeriodInMilliseconds = requestPeriodInMilliseconds;
  }

  public void start(Consumer<EVENT> eventConsumer) {
    watcherRunning = true;

    new Thread() {
      @Override
      public void run() {

        while (watcherRunning) {
          try {

            List<EVENT> eventsToPublish = pollingDao.findEventsToPublish();

            eventsToPublish.forEach(eventConsumer::accept);

            if (!eventsToPublish.isEmpty()) {

              pollingDao.markEventsAsPublished(eventsToPublish);
            }

            try {
              Thread.sleep(requestPeriodInMilliseconds);
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
    watcherRunning = false;
  }
}
