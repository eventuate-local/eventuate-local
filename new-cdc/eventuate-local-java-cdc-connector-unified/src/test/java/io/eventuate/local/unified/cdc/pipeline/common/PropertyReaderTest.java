package io.eventuate.local.unified.cdc.pipeline.common;

import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class PropertyReaderTest {

  private PropertyReader propertyReader = new PropertyReader();

  @Test
  public void testCorrectProperties() {
    List<Map<String, Object>> propertyMaps = propertyReader
            .convertPropertiesToListOfMaps("[{\"type\" : \"mysql\", \"reader\" : \"reader1\"}]");

    Assert.assertEquals(1, propertyMaps.size());

    Map<String, Object> props = propertyMaps.get(0);

    propertyReader.checkForUnknownProperties(props, CdcPipelineProperties.class);

    CdcPipelineProperties cdcPipelineProperties = propertyReader
            .convertMapToPropertyClass(props, CdcPipelineProperties.class);

    Assert.assertEquals("mysql", cdcPipelineProperties.getType());
    Assert.assertEquals("reader1", cdcPipelineProperties.getReader());
  }

  @Test
  public void testUnknownProperties() {
    List<Map<String, Object>> propertyMaps = propertyReader
            .convertPropertiesToListOfMaps("[{\"type\" : \"mysql\", \"reader\" : \"reader1\", \"somepropname\" : \"somepropvalue\"}]");

    Assert.assertEquals(1, propertyMaps.size());

    Map<String, Object> props = propertyMaps.get(0);

    Exception exception = null;

    try {
      propertyReader.checkForUnknownProperties(props, CdcPipelineProperties.class);
    } catch (Exception e) {
      exception = e;
    }

    Assert.assertNotNull(exception);
    Assert.assertEquals("Unknown properties: [somepropname]", exception.getMessage());
  }
}
