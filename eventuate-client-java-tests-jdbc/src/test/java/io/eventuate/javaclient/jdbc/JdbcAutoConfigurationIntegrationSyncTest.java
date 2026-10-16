package io.eventuate.javaclient.jdbc;

import io.eventuate.example.banking.services.counting.InvocationCounter;
import io.eventuate.javaclient.spring.tests.common.AbstractSpringAccountIntegrationSyncTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = JdbcAutoConfigurationIntegrationTestConfiguration.class)
public class JdbcAutoConfigurationIntegrationSyncTest extends AbstractSpringAccountIntegrationSyncTest {

  @Autowired
  private InvocationCounter invocationCounter;

  @Override
  @Test
  public void shouldStartMoneyTransfer() throws ExecutionException, InterruptedException {
    super.shouldStartMoneyTransfer();
    assertTrue(invocationCounter.get() > 0, "Expected invocation");
  }
}
