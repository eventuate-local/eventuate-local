package io.eventuate.local.unified.cdc.pipeline;

import io.eventuate.local.unified.cdc.pipeline.common.configuration.CommonCdcPipelineConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogCdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalCdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingCdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingCdcPipelineFactoryConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonCdcPipelineConfiguration.class,

        MySqlBinlogCdcDefaultPipelinePropertiesConfiguration.class,
        MySqlBinlogCdcPipelineFactoryConfiguration.class,

        PollingCdcDefaultPipelinePropertiesConfiguration.class,
        PollingCdcPipelineFactoryConfiguration.class,

        PostgresWalCdcDefaultPipelinePropertiesConfiguration.class,
        PostgresWalCdcPipelineFactoryConfiguration.class,
        CdcPipelineConfiguration.class})
public class UnifiedCdcConnectorConfiguration {
}
