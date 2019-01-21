package io.eventuate.local.unified.cdc.pipeline.common;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;

import java.beans.FeatureDescriptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PropertyReader {
  private ObjectMapper objectMapper = new ObjectMapper();

  {
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
  }

  public PropertyReader() {
  }

  public List<Map<String, Object>> convertPropertiesToListOfMaps(String properties) {
    try {
      return objectMapper.readValue(properties, List.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public <PROPERTIES> PROPERTIES convertMapToPropertyClass(Map<String, Object> properties,
                                                           Class<PROPERTIES> propertyClass) {

    return objectMapper.convertValue(properties, propertyClass);
  }

  public void checkForUnknownProperties(Map<String, Object> properties, Class<?> propertyClass) {
    List<String> propNames = Arrays
            .stream(BeanUtils.getPropertyDescriptors(propertyClass))
            .map(FeatureDescriptor::getName)
            .map(String::toLowerCase)
            .collect(Collectors.toList());

    List<String> unexpectedProperties =
            properties
                    .keySet()
                    .stream()
                    .map(String::toLowerCase)
                    .filter(p -> !propNames.contains(p))
                    .collect(Collectors.toList());

    if (!unexpectedProperties.isEmpty()) {
      String unexpectedPropertiesString = unexpectedProperties.stream().collect(Collectors.joining(", ", "[", "]"));

      throw new RuntimeException(String.format("Unknown properties: %s", unexpectedPropertiesString));
    }
  }
}
