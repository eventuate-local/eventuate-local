package io.eventuate.local.unified.cdc;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CdcPipelineConfiguration.class, CdcPipelineFactoryConfiguration.class})
public class UnifiedCdcConfiguration {
}
