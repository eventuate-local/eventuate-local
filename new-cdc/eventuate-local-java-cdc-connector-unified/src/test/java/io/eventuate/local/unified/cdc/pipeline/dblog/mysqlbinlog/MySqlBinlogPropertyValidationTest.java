package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog;

import io.eventuate.local.unified.cdc.pipeline.dblog.common.CommonDbLogPropertyValidationTest;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.MySqlBinlogCdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineProperties;
import org.junit.Assert;
import org.junit.Test;

public class MySqlBinlogPropertyValidationTest extends CommonDbLogPropertyValidationTest {

  @Test
  public void testMySqlBinlogProperties() throws Exception {
    PropertyBuilder propertyBuilder = new PropertyBuilder();

    assertExceptionMessage(propertyBuilder.toString(), MySqlBinlogCdcPipelineProperties.class, "type must not be null");

    propertyBuilder.addString("type", MySqlBinlogCdcPipelineFactory.TYPE);

    testCommonRequiredProperties(MySqlBinlogCdcPipelineProperties.class, propertyBuilder);

    propertyBuilder.addString("cdcDbUserName", "rootUser");
    assertExceptionMessage(propertyBuilder.toString(), MySqlBinlogCdcPipelineProperties.class, "cdcDbPassword must not be null");

    propertyBuilder.addString("cdcDbPassword", "rootUser");
    assertNoException(propertyBuilder.toString(), MySqlBinlogCdcPipelineProperties.class);

    assertNoException(propertyBuilder.toString(), MySqlBinlogCdcPipelineProperties.class);

    MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties = objectMapper.readValue(propertyBuilder.toString(), MySqlBinlogCdcPipelineProperties.class);

    testCommonDbLogDefaultProperties(mySqlBinlogCdcPipelineProperties);

    Assert.assertNotNull(mySqlBinlogCdcPipelineProperties.getBinlogClientId());
    Assert.assertEquals("eventuate.local.cdc.my-sql-connector.offset.storage", mySqlBinlogCdcPipelineProperties.getOldDbHistoryTopicName());
  }
}
