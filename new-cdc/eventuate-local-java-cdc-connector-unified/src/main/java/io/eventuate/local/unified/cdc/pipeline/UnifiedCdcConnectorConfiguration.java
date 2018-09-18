package io.eventuate.local.unified.cdc.pipeline;

import io.eventuate.local.unified.cdc.pipeline.common.configuration.CommonCdcPipelineConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.configuration.CommonDbLogCdcPilpelineConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.*;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.*;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonCdcPipelineConfiguration.class,

        CommonDbLogCdcPilpelineConfiguration.class,

        MySqlBinlogDefaultCdcPipelinePropertiesConfiguration.class,
        MySqlBinlogCdcPipelineFactoryConfiguration.class,
        MySqlBinlogCdcPipelineReaderFactoryConfiguration.class,
        MySqlBinlogDefaultCdcPipelineFactoryConfiguration.class,
        MySqlBinlogDefaultCdcPipelineReaderFactoryConfiguration.class,

        PollingDefaultCdcPipelinePropertiesConfiguration.class,
        PollingCdcPipelineFactoryConfiguration.class,
        PollingCdcPipelineReaderFactoryConfiguration.class,
        PollingDefaultCdcPipelineFactoryConfiguration.class,
        PollingDefaultCdcPipelineReaderFactoryConfiguration.class,

        PostgresWalDefaultCdcPipelinePropertiesConfiguration.class,
        PostgresWalCdcPipelineFactoryConfiguration.class,
        PostgresWalCdcPipelineReaderFactoryConfiguration.class,
        PostgresWalDefaultCdcPipelineFactoryConfiguration.class,
        PostgresWalDefaultCdcPipelineReaderFactoryConfiguration.class,

        CdcPipelineConfiguration.class})
public class UnifiedCdcConnectorConfiguration {
}
