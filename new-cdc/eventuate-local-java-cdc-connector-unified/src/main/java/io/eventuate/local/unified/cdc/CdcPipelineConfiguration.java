package io.eventuate.local.unified.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.eventuate.local.unified.cdc.factory.CdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.CdcPipeline;
import io.eventuate.local.unified.cdc.properties.CdcPipelineProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

public class CdcPipelineConfiguration {
  private ObjectMapper objectMapper = new ObjectMapper();
  private List<CdcPipeline> cdcPipelines = new ArrayList<>();

  @PostConstruct
  @Autowired
  public void initialize(@Value("${eventuate.cdc.pipeline.properties#{null}}") String cdcPipelineJsonProperties,
                         Collection<CdcPipelineFactory> cdcPipelineFactories) {
    Optional
            .ofNullable(cdcPipelineJsonProperties)
            .map(this::convertCdcPipelinePropertiesToListOfMaps)
            .orElse(Collections.emptyList())
            .stream()
            .forEach(properties -> createStartSaveCdcPipeline(cdcPipelineFactories, properties));
  }

  @PreDestroy
  public void stop() {
    cdcPipelines.forEach(CdcPipeline::stop);
  }

  private List<Map<String, Object>> convertCdcPipelinePropertiesToListOfMaps(String properties) {
    try {
      return objectMapper.readValue(properties, List.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void createStartSaveCdcPipeline(Collection<CdcPipelineFactory> cdcPipelineFactories,
                                          Map<String, Object> properties) {

    CdcPipeline cdcPipeline = createCdcPipeline(cdcPipelineFactories, properties);
    cdcPipeline.start();
    cdcPipelines.add(cdcPipeline);
  }

  private CdcPipeline createCdcPipeline(Collection<CdcPipelineFactory> cdcPipelineFactories,
                                        Map<String, Object> properties) {

    CdcPipelineProperties cdcPipelineProperties = objectMapper.convertValue(properties, CdcPipelineProperties.class);

    CdcPipelineFactory<? extends CdcPipelineProperties> cdcPipelineFactory = findCdcPipelineFactory(cdcPipelineFactories, cdcPipelineProperties);

    CdcPipelineProperties exactCdcPipelineProperties = objectMapper.convertValue(properties, cdcPipelineFactory.propertyClass());

    return  ((CdcPipelineFactory)cdcPipelineFactory).create(exactCdcPipelineProperties);
  }

  private CdcPipelineFactory<? extends CdcPipelineProperties> findCdcPipelineFactory(Collection<CdcPipelineFactory> cdcPipelineFactories,
                                                                                     CdcPipelineProperties cdcPipelineProperties) {
    return cdcPipelineFactories
            .stream()
            .filter(factory ->  factory.supports(cdcPipelineProperties.getType()))
            .findAny()
            .orElseThrow(() ->
                    new RuntimeException(String.format("factory not found for type %s",
                            cdcPipelineProperties.getType())));
  }
}
