package io.eventuate.local.unified.cdc.pipeline;

import io.eventuate.local.unified.cdc.pipeline.common.configuration.CdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.common.configuration.CommonCdcPipelineConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.*;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.*;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonCdcPipelineConfiguration.class,

        CdcDefaultPipelinePropertiesConfiguration.class,

        MySqlBinlogDefaultCdcPipelineReaderConfiguration.class,
        MySqlBinlogCdcPipelineFactoryConfiguration.class,
        MySqlBinlogDefaultCdcPipelineFactoryConfiguration.class,

        PollingDefaultCdcPipelineReaderConfiguration.class,
        PollingCdcPipelineFactoryConfiguration.class,
        PollingDefaultCdcPipelineFactoryConfiguration.class,

        PostgresWalDefaultCdcPipelineReaderConfiguration.class,
        PostgresWalCdcPipelineFactoryConfiguration.class,
        PostgresWalDefaultCdcPipelineFactoryConfiguration.class,

        CdcPipelineConfiguration.class})
public class UnifiedCdcConnectorConfiguration {
}
