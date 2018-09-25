package io.eventuate.local.unified.cdc.pipeline;

import io.eventuate.local.unified.cdc.pipeline.common.configuration.CdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.common.configuration.CommonCdcPipelineConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogDefaultCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogDefaultCdcPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalDefaultCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalDefaultCdcPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingDefaultCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingDefaultCdcPipelineReaderConfiguration;
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
