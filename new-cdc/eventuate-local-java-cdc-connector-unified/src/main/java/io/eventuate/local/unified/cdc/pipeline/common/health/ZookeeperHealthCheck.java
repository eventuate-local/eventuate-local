package io.eventuate.local.unified.cdc.pipeline.common.health;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;

import java.util.Collections;
import java.util.List;

public class ZookeeperHealthCheck extends AbstractHealthCheck {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${eventuatelocal.zookeeper.connection.string}")
  private String zkUrl;

  @Override
  protected void determineHealth(HealthBuilder builder) {
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zkUrl, 1000, 1000, new RetryNTimes(0, 0));

    try {
      curatorFramework.start();
      curatorFramework.checkExists().forPath("/some/test/path");
      builder.addDetail("Connected to Zookeeper");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      builder.addError("Connection to zookeeper failed");
    } finally {
      try {
        curatorFramework.close();
      } catch (Exception ce) {
        logger.error(ce.getMessage(), ce);
      }
    }
  }

}
