package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog;

import io.eventuate.local.unified.cdc.pipeline.dblog.common.CommonDbLogPipelineReaderPropertyValidationTest;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.MySqlBinlogCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineReaderProperties;
import org.junit.Assert;
import org.junit.Test;

public class MySqlBinlogPipelineReaderPropertyValidationTest extends CommonDbLogPipelineReaderPropertyValidationTest {

  @Test
  public void testMySqlBinlogProperties() throws Exception {
    PropertyBuilder propertyBuilder = new PropertyBuilder();

    assertExceptionMessage(propertyBuilder.toString(), MySqlBinlogCdcPipelineReaderProperties.class, "type must not be null");

    propertyBuilder.addString("type", MySqlBinlogCdcPipelineReaderFactory.TYPE);

    testCommonRequiredProperties(MySqlBinlogCdcPipelineReaderProperties.class, propertyBuilder);

    propertyBuilder.addString("cdcDbUserName", "rootUser");
    assertExceptionMessage(propertyBuilder.toString(), MySqlBinlogCdcPipelineReaderProperties.class, "cdcDbPassword must not be null");

    propertyBuilder.addString("cdcDbPassword", "rootUser");
    assertExceptionMessage(propertyBuilder.toString(), MySqlBinlogCdcPipelineReaderProperties.class, "mySqlBinlogClientUniqueId must not be null");

    propertyBuilder.addString("mySqlBinlogClientUniqueId", String.valueOf(System.nanoTime()));
    assertExceptionMessage(propertyBuilder.toString(), MySqlBinlogCdcPipelineReaderProperties.class, "readOldDebeziumDbOffsetStorageTopic must not be null");

    propertyBuilder.addString("readOldDebeziumDbOffsetStorageTopic", "false");
    assertNoException(propertyBuilder.toString(), MySqlBinlogCdcPipelineReaderProperties.class);

    MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties =
            objectMapper.readValue(propertyBuilder.toString(), MySqlBinlogCdcPipelineReaderProperties.class);

    testCommonDbLogDefaultProperties(mySqlBinlogCdcPipelineReaderProperties);
  }
}
