package io.eventuate.local.unified.cdc.pipeline.dblog.common;

import io.eventuate.local.unified.cdc.pipeline.common.CommonPipelinePropertyValidationTest;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.properties.CommonDbLogCdcPipelineProperties;
import org.junit.Assert;

public class CommonDbLogPipelinePropertyValidationTest extends CommonPipelinePropertyValidationTest {
  protected void testCommonDbLogDefaultProperties(CommonDbLogCdcPipelineProperties commonDbLogCdcPipelineProperties) {
    Assert.assertEquals("db.history.topic", commonDbLogCdcPipelineProperties.getDbHistoryTopicName());
  }
}
