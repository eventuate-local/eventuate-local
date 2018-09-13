package io.eventuate.local.unified.cdc.pipeline;

import io.eventuate.local.unified.cdc.pipeline.common.configuration.CommonCdcPipelineConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.configuration.CommonDbLogCdcPilpelineConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogDefaultCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogDefaultCdcPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalDefaultCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalDefaultCdcPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingDefaultCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingDefaultCdcPipelinePropertiesConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonCdcPipelineConfiguration.class,

        CommonDbLogCdcPilpelineConfiguration.class,

        MySqlBinlogDefaultCdcPipelinePropertiesConfiguration.class,
        MySqlBinlogCdcPipelineFactoryConfiguration.class,
        MySqlBinlogDefaultCdcPipelineFactoryConfiguration.class,

        PollingDefaultCdcPipelinePropertiesConfiguration.class,
        PollingCdcPipelineFactoryConfiguration.class,
        PollingDefaultCdcPipelineFactoryConfiguration.class,

        PostgresWalDefaultCdcPipelinePropertiesConfiguration.class,
        PostgresWalCdcPipelineFactoryConfiguration.class,
        PostgresWalDefaultCdcPipelineFactoryConfiguration.class,

        CdcPipelineConfiguration.class})
public class UnifiedCdcConnectorConfiguration {
}
