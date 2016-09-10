package io.eventuate.local.cdc.debezium;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LeadershipElectionTest.LeadershipElectionTestConfiguration.class)
@DirtiesContext
@IntegrationTest
public class LeadershipElectionTest {

  @Autowired
  private EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties;

  @Test
  public void shouldElectLeader() throws InterruptedException {
    CuratorFramework client1 = EventTableChangesToAggregateTopicRelayConfiguration.makeStartedCuratorClient(eventuateLocalZookeperConfigurationProperties.getConnectionString());
    CuratorFramework client2 = EventTableChangesToAggregateTopicRelayConfiguration.makeStartedCuratorClient(eventuateLocalZookeperConfigurationProperties.getConnectionString());

    //String leaderPath = "/eventuatelocal/cdc/testleader";
    String leaderPath = "/foo";

    MyLeaderSelectorListener myLeaderSelectorListener1 = new MyLeaderSelectorListener("1");
    LeaderSelector leaderSelector1 = new LeaderSelector(client1, leaderPath, myLeaderSelectorListener1);
    leaderSelector1.start();

    MyLeaderSelectorListener myLeaderSelectorListener2 = new MyLeaderSelectorListener("2");
    LeaderSelector leaderSelector2 = new LeaderSelector(client2, leaderPath, myLeaderSelectorListener2);
    leaderSelector2.start();

    TimeUnit.SECONDS.sleep(5);
    assertEquals(1L, MyLeaderSelectorListener.leaderCounter.get());

    TimeUnit.SECONDS.sleep(120);
  }


  @Configuration
  @EnableConfigurationProperties(EventuateLocalZookeperConfigurationProperties.class)
  static public class LeadershipElectionTestConfiguration {
  }
}

class MyLeaderSelectorListener implements LeaderSelectorListener {

  AtomicBoolean leader = new AtomicBoolean(false);
  static AtomicLong leaderCounter = new AtomicLong(0);

  private String label;

  public MyLeaderSelectorListener(String label) {
    this.label = label;
  }


  @Override
  public void takeLeadership(CuratorFramework client) throws Exception {
    takeLeadership();
  }

  private void takeLeadership() {
    System.out.println("take leadership: " + label);
    leader.set(true);
    leaderCounter.incrementAndGet();
    try {
      TimeUnit.SECONDS.sleep(500);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void releaseLeadership() {
    System.out.println("false leadership: " + label);
    leader.set(false);
    leaderCounter.decrementAndGet();
  }

  @Override
  public void stateChanged(CuratorFramework client, ConnectionState newState) {
    System.out.println("StateChanged: " + newState);
    switch (newState) {
      case SUSPENDED:
        releaseLeadership();
        break;

      case RECONNECTED:
        takeLeadership();
        break;

      case LOST:
        releaseLeadership();
        break;
    }

  }

}

