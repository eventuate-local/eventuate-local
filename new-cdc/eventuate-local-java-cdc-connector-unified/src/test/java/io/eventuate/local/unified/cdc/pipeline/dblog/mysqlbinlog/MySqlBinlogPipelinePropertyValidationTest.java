package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog;

import io.eventuate.local.unified.cdc.pipeline.common.CommonPipelinePropertyValidationTest;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.MySqlBinlogCdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineProperties;
import org.junit.Test;

public class MySqlBinlogPipelinePropertyValidationTest extends CommonPipelinePropertyValidationTest {

  @Test
  public void testMySqlBinlogProperties() throws Exception {
    PropertyBuilder propertyBuilder = new PropertyBuilder();

    assertExceptionMessage(propertyBuilder.toString(), MySqlBinlogCdcPipelineProperties.class, "type must not be null");

    propertyBuilder.addString("type", MySqlBinlogCdcPipelineFactory.TYPE);

    testCommonRequiredProperties(MySqlBinlogCdcPipelineProperties.class, propertyBuilder);

    assertNoException(propertyBuilder.toString(), MySqlBinlogCdcPipelineProperties.class);
  }
}
