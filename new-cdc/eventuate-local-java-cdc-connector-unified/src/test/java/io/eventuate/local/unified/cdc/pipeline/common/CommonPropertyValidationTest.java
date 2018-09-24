package io.eventuate.local.unified.cdc.pipeline.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.eventuate.local.unified.cdc.pipeline.common.properties.ValidatableProperties;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class CommonPropertyValidationTest {

  protected ObjectMapper objectMapper = new ObjectMapper();

  protected  <PROPERTIES extends ValidatableProperties> void assertExceptionMessage(String properties,
                                                                                    Class<PROPERTIES> propertyClass,
                                                                                    String message) throws Exception {
    assertExceptionMessage(properties, propertyClass, Optional.of(message));
  }

  protected <PROPERTIES extends ValidatableProperties> void assertNoException(String properties,
                                                                            Class<PROPERTIES> propertyClass) throws Exception {
    assertExceptionMessage(properties, propertyClass, Optional.empty());
  }

  protected <PROPERTIES extends ValidatableProperties> void assertExceptionMessage(String properties,
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
