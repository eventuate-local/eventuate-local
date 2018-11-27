package io.eventuate.local.unified.cdc.pipeline.dblog.common;

import io.eventuate.local.unified.cdc.pipeline.common.CommonPipelineReaderPropertyValidationTest;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.properties.CommonDbLogCdcPipelineReaderProperties;
import org.junit.Assert;

public class CommonDbLogPipelineReaderPropertyValidationTest extends CommonPipelineReaderPropertyValidationTest {
  protected void testCommonDbLogDefaultProperties(CommonDbLogCdcPipelineReaderProperties commonDbLogCdcPipelineReaderProperties) {
    Assert.assertEquals(5000, (int)commonDbLogCdcPipelineReaderProperties.getBinlogConnectionTimeoutInMilliseconds());
    Assert.assertEquals(100, (int)commonDbLogCdcPipelineReaderProperties.getMaxAttemptsForBinlogConnection());
  }
}
