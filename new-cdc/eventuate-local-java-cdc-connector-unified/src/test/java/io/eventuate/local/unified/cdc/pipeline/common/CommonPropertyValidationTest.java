package io.eventuate.local.unified.cdc.pipeline.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class CommonPropertyValidationTest {

  protected ObjectMapper objectMapper = new ObjectMapper();

  protected  <PROPERTIES extends CdcPipelineProperties> void testCommonRequiredProperties(Class<PROPERTIES> propertyClass,
                                                                                          PropertyBuilder propertyBuilder) throws Exception {

    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "reader must not be null");
    propertyBuilder.addString("reader", "reader1");

    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "dataSourceUrl must not be null");

    propertyBuilder.addString("dataSourceUrl", "jdbc:correctdb://localhost/eventuate");
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "dataSourceUserName must not be null");

    propertyBuilder.addString("dataSourceUserName", "testUser");
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "dataSourcePassword must not be null");

    propertyBuilder.addString("dataSourcePassword", "testPassword");
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "dataSourceDriverClassName must not be null");

    propertyBuilder.addString("dataSourceDriverClassName", "com.correct.db.Driver");
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "leadershipLockPath must not be null");

    propertyBuilder.addString("leadershipLockPath", "/eventuate/leader/test");
  }

  protected  <PROPERTIES extends CdcPipelineProperties> void assertExceptionMessage(String properties,
                                                                                 Class<PROPERTIES> propertyClass,
                                                                                 String message) throws Exception {
    assertExceptionMessage(properties, propertyClass, Optional.of(message));
  }

  protected <PROPERTIES extends CdcPipelineProperties> void assertNoException(String properties,
                                                                            Class<PROPERTIES> propertyClass) throws Exception {
    assertExceptionMessage(properties, propertyClass, Optional.empty());
  }

  protected <PROPERTIES extends CdcPipelineProperties> void assertExceptionMessage(String properties,
                                                                                 Class<PROPERTIES> propertyClass,
                                                                                 Optional<String> message) throws Exception {

    PROPERTIES cdcPipelineProperties = objectMapper.readValue(properties, propertyClass);

    Exception exception = null;

    try {
      cdcPipelineProperties.validate();
    } catch (IllegalArgumentException e) {
      exception = e;
    }

    Exception ee = exception;

    message.map(msg -> {
      Assert.assertNotNull(ee);
      Assert.assertEquals(msg, ee.getMessage());
      return msg;
    }).orElseGet(() -> {
      Assert.assertNull(ee);
      return null;
    });
  }

  public static class PropertyBuilder {
    private List<Entry> entries = new ArrayList<>();

    public void addString(String key, String value) {
      entries.add(new Entry(key, String.format("\"%s\"", value)));
    }

    @Override
    public String toString() {
      StringJoiner stringJoiner = new StringJoiner(", ");
      entries.forEach(e -> stringJoiner.add(e.toString()));
      return String.format("{%s}", stringJoiner.toString());
    }

    private static class Entry {
      final String key;
      final String value;

      public Entry(String key, String value) {
        this.key = key;
        this.value = value;
      }

      @Override
      public String toString() {
        return String.format("\"%s\" : %s", key, value);
      }
    }
  }
}
