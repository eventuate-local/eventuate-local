package io.eventuate.local.unified.cdc.pipeline;

import io.eventuate.local.common.BinlogEntryReader;
import io.eventuate.local.common.PublishedEvent;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.CdcPipeline;
import io.eventuate.local.unified.cdc.pipeline.common.PropertyReader;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineProperties;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

@Configuration
public class CdcPipelineConfiguration {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private PropertyReader propertyReader = new PropertyReader();

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
            .map(propertyReader::convertPropertiesToListOfMaps)
            .orElseGet(() -> {
              createStartSaveCdcDefaultPipelineReader(defaultCdcPipelineReaderProperties);
              return Collections.emptyList();
            })
            .forEach(this::createCdcPipelineReader);


    Optional
            .ofNullable(cdcPipelineJsonProperties)
            .map(propertyReader::convertPropertiesToListOfMaps)
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

  private void createStartSaveCdcPipeline(Map<String, Object> properties) {
    propertyReader.checkForUnknownProperties(properties, CdcPipelineProperties.class);

    CdcPipelineProperties cdcPipelineProperties = propertyReader
            .convertMapToPropertyClass(properties, CdcPipelineProperties.class);

    cdcPipelineProperties.validate();

    CdcPipeline cdcPipeline = createCdcPipeline(cdcPipelineProperties);
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

    binlogEntryReaderProvider.addReader(cdcDefaultPipelineReaderProperties.getName(),
            cdcDefaultPipelineReaderProperties.getType(),
            binlogEntryReader);
  }

  private CdcPipeline<?> createCdcPipeline(CdcPipelineProperties properties) {

    CdcPipelineFactory<?> cdcPipelineFactory =
            findCdcPipelineFactory(properties.getType(), binlogEntryReaderProvider.getReaderType(properties.getReader()));

    return cdcPipelineFactory.create(properties);
  }

  private void createCdcPipelineReader(Map<String, Object> properties) {

    CdcPipelineReaderProperties cdcPipelineReaderProperties = propertyReader
            .convertMapToPropertyClass(properties, CdcPipelineReaderProperties.class);
    cdcPipelineReaderProperties.validate();

    CdcPipelineReaderFactory<? extends CdcPipelineReaderProperties, ? extends BinlogEntryReader> cdcPipelineReaderFactory =
            findCdcPipelineReaderFactory(cdcPipelineReaderProperties.getType());

    propertyReader.checkForUnknownProperties(properties, cdcPipelineReaderFactory.propertyClass());

    CdcPipelineReaderProperties exactCdcPipelineReaderProperties = propertyReader
            .convertMapToPropertyClass(properties, cdcPipelineReaderFactory.propertyClass());
    exactCdcPipelineReaderProperties.validate();

    binlogEntryReaderProvider.addReader(cdcPipelineReaderProperties.getName(),
            cdcPipelineReaderProperties.getType(),
            ((CdcPipelineReaderFactory)cdcPipelineReaderFactory).create(exactCdcPipelineReaderProperties));
  }

  private CdcPipelineFactory<PublishedEvent> findCdcPipelineFactory(String type, String readerType) {
    return cdcPipelineFactories
            .stream()
            .filter(factory ->  factory.supports(type, readerType))
            .findAny()
            .orElseThrow(() ->
                    new RuntimeException(String.format("reader factory not found for type %s",
                            type)));
  }

  private CdcPipelineReaderFactory<? extends CdcPipelineReaderProperties, BinlogEntryReader> findCdcPipelineReaderFactory(String type) {
    return cdcPipelineReaderFactories
            .stream()
            .filter(factory ->  factory.supports(type))
            .findAny()
            .orElseThrow(() ->
                    new RuntimeException(String.format("pipeline factory not found for type %s",
                            type)));
  }
}
