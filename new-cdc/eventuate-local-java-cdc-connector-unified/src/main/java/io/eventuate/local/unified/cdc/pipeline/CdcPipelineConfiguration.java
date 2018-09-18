package io.eventuate.local.unified.cdc.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.eventuate.local.common.BinlogEntryReader;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

@Configuration
public class CdcPipelineConfiguration {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private ObjectMapper objectMapper = new ObjectMapper();
  private List<CdcPipeline> cdcPipelines = new ArrayList<>();

  @Value("${eventuate.cdc.pipeline.properties:#{null}}")
  private String cdcPipelineJsonProperties;

  @Value("${eventuate.cdc.pipeline.reader.properties:#{null}}")
  private String cdcPipelineReaderJsonProperties;

  @Autowired
  private Collection<CdcPipelineFactory> cdcPipelineFactories;

  @Autowired
  private Collection<CdcPipelineReaderFactory> cdcPipelineReaderFactories;

  @Autowired
  @Qualifier("defaultCdcPipelineFactory")
  private CdcPipelineFactory defaultCdcPipelineFactory;

  @Autowired
  @Qualifier("defaultCdcPipelineReaderFactory")
  private CdcPipelineReaderFactory defaultCdcPipelineReaderFactory;

  @Autowired
  private CdcPipelineProperties defaultCdcPipelineProperties;

  @Autowired
  private CdcPipelineReaderProperties defaultCdcPipelineReaderProperties;

  @Autowired
  private BinlogEntryReaderProvider binlogEntryReaderProvider;

  @PostConstruct
  public void initialize() {
    logger.info("Starting unified cdc pipelines");

    Optional
            .ofNullable(cdcPipelineReaderJsonProperties)
            .map(this::convertPropertiesToListOfMaps)
            .orElseGet(() -> {
              createStartSaveCdcDefaultPipelineReader(defaultCdcPipelineReaderProperties);
              return Collections.emptyList();
            })
            .forEach(this::createCdcPipelineReader);


    Optional
            .ofNullable(cdcPipelineJsonProperties)
            .map(this::convertPropertiesToListOfMaps)
            .orElseGet(() -> {
              createStartSaveCdcDefaultPipeline(defaultCdcPipelineProperties);
              return Collections.emptyList();
            })
            .forEach(this::createStartSaveCdcPipeline);

    binlogEntryReaderProvider.start();

    logger.info("Unified cdc pipelines are started");
  }

  @PreDestroy
  public void stop() {
    binlogEntryReaderProvider.stop();

    cdcPipelines.forEach(CdcPipeline::stop);
  }

  private List<Map<String, Object>> convertPropertiesToListOfMaps(String properties) {
    try {
      return objectMapper.readValue(properties, List.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void createStartSaveCdcPipeline(Map<String, Object> properties) {

    CdcPipeline cdcPipeline = createCdcPipeline(properties);
    cdcPipeline.start();
    cdcPipelines.add(cdcPipeline);
  }

  private void createStartSaveCdcDefaultPipeline(CdcPipelineProperties cdcDefaultPipelineProperties) {

    cdcDefaultPipelineProperties.validate();
    CdcPipeline cdcPipeline = defaultCdcPipelineFactory.create(cdcDefaultPipelineProperties);
    cdcPipeline.start();
    cdcPipelines.add(cdcPipeline);
  }

  private void createStartSaveCdcDefaultPipelineReader(CdcPipelineReaderProperties cdcDefaultPipelineReaderProperties) {
    cdcDefaultPipelineReaderProperties.validate();
    BinlogEntryReader binlogEntryReader = defaultCdcPipelineReaderFactory.create(cdcDefaultPipelineReaderProperties);
    binlogEntryReaderProvider.addReader(cdcDefaultPipelineReaderProperties.getName(), binlogEntryReader);
  }

  private CdcPipeline createCdcPipeline(Map<String, Object> properties) {

    CdcPipelineProperties cdcPipelineProperties = objectMapper.convertValue(properties, CdcPipelineProperties.class);
    cdcPipelineProperties.validate();

    CdcPipelineFactory<? extends CdcPipelineProperties, PublishedEvent> cdcPipelineFactory = findCdcPipelineFactory(cdcPipelineProperties.getType());

    CdcPipelineProperties exactCdcPipelineProperties = objectMapper.convertValue(properties, cdcPipelineFactory.propertyClass());
    exactCdcPipelineProperties.validate();

    return  ((CdcPipelineFactory)cdcPipelineFactory).create(exactCdcPipelineProperties);
  }

  private void createCdcPipelineReader(Map<String, Object> properties) {

    CdcPipelineReaderProperties cdcPipelineReaderProperties = objectMapper.convertValue(properties, CdcPipelineReaderProperties.class);
    cdcPipelineReaderProperties.validate();

    CdcPipelineReaderFactory<? extends CdcPipelineReaderProperties, ? extends BinlogEntryReader> cdcPipelineReaderFactory =
            findCdcPipelineReaderFactory(cdcPipelineReaderProperties.getType());

    CdcPipelineReaderProperties exactCdcPipelineReaderProperties = objectMapper.convertValue(properties, cdcPipelineReaderFactory.propertyClass());
    exactCdcPipelineReaderProperties.validate();

    binlogEntryReaderProvider.addReader(cdcPipelineReaderProperties.getName(),
            ((CdcPipelineReaderFactory)cdcPipelineReaderFactory).create(exactCdcPipelineReaderProperties));
  }

  private CdcPipelineFactory<? extends CdcPipelineProperties, PublishedEvent> findCdcPipelineFactory(String type) {
    return cdcPipelineFactories
            .stream()
            .filter(factory ->  factory.supports(type))
            .findAny()
            .orElseThrow(() ->
                    new RuntimeException(String.format("factory not found for type %s",
                            type)));
  }

  private CdcPipelineReaderFactory<? extends CdcPipelineReaderProperties, BinlogEntryReader> findCdcPipelineReaderFactory(String type) {
    return cdcPipelineReaderFactories
            .stream()
            .filter(factory ->  factory.supports(type))
            .findAny()
            .orElseThrow(() ->
                    new RuntimeException(String.format("factory not found for type %s",
                            type)));
  }
}
