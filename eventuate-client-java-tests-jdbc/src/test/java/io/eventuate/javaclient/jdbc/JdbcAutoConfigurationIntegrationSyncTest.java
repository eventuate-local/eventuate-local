package io.eventuate.javaclient.jdbc;

import io.eventuate.example.banking.services.counting.InvocationCounter;
import io.eventuate.javaclient.spring.tests.common.AbstractSpringAccountIntegrationSyncTest;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = JdbcAutoConfigurationIntegrationTestConfiguration.class)
public class JdbcAutoConfigurationIntegrationSyncTest extends AbstractSpringAccountIntegrationSyncTest {

  @Autowired
  private InvocationCounter invocationCounter;

  @Override
  public void shouldStartMoneyTransfer() throws ExecutionException, InterruptedException {
    super.shouldStartMoneyTransfer();
    assertTrue("Expected invocation", invocationCounter.get() > 0);
  }
}
