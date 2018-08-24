package io.eventuate.local.unified.cdc.pipeline.dblog.common;

import io.eventuate.local.unified.cdc.pipeline.common.CommonPropertyValidationTest;
import org.junit.Assert;

public class CommonDbLogPropertyValidationTest extends CommonPropertyValidationTest {
  protected void testCommonDbLogDefaultProperties(CommonDbLogCdcPipelineProperties commonDbLogCdcPipelineProperties) {
    Assert.assertEquals("db.history.topic", commonDbLogCdcPipelineProperties.getDbHistoryTopicName());
    Assert.assertEquals("MySqlBinLog", commonDbLogCdcPipelineProperties.getMySqlBinLogClientName());
    Assert.assertEquals(5000, (int)commonDbLogCdcPipelineProperties.getBinlogConnectionTimeoutInMilliseconds());
    Assert.assertEquals(100, (int)commonDbLogCdcPipelineProperties.getMaxAttemptsForBinlogConnection());
  }
}
