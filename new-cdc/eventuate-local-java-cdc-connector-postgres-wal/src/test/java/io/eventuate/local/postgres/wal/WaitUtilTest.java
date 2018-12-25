package io.eventuate.local.postgres.wal;

import org.junit.Assert;
import org.junit.Test;

public class WaitUtilTest {

  @Test
  public void testSuccessfulWaiting() throws InterruptedException {
    WaitUtil waitUtil = new WaitUtil(10000);

    Assert.assertTrue(waitUtil.start());
    Assert.assertFalse(waitUtil.start());

    Thread.sleep(1);
    Assert.assertTrue(waitUtil.tick());
    Assert.assertTrue(waitUtil.isWaiting());

    Thread.sleep(1);
    Assert.assertTrue(waitUtil.tick());
    Assert.assertTrue(waitUtil.isWaiting());
  }

  @Test
  public void testFailedWaiting() throws InterruptedException {
    WaitUtil waitUtil = new WaitUtil(1);
    waitUtil.start();
    Thread.sleep(10);
    Assert.assertFalse(waitUtil.tick());
    Assert.assertFalse(waitUtil.isWaiting());
  }

}