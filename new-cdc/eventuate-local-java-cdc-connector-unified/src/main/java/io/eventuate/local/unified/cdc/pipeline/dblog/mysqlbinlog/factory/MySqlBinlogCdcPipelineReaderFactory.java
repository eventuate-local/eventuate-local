package io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory;

import io.eventuate.local.common.JdbcUrl;
import io.eventuate.local.common.JdbcUrlParser;
import io.eventuate.local.mysql.binlog.MySqlBinaryLogClient;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CommonCdcPipelineReaderFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.properties.MySqlBinlogCdcPipelineReaderProperties;
import org.apache.curator.framework.CuratorFramework;

public class MySqlBinlogCdcPipelineReaderFactory extends CommonCdcPipelineReaderFactory<MySqlBinlogCdcPipelineReaderProperties, MySqlBinaryLogClient> {
  public static final String TYPE = "mysql-binlog";


  public MySqlBinlogCdcPipelineReaderFactory(CuratorFramework curatorFramework, BinlogEntryReaderProvider binlogEntryReaderProvider) {
    super(curatorFramework, binlogEntryReaderProvider);
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }

  @Override
  public Class<MySqlBinlogCdcPipelineReaderProperties> propertyClass() {
    return MySqlBinlogCdcPipelineReaderProperties.class;
  }

  @Override
  public MySqlBinaryLogClient create(MySqlBinlogCdcPipelineReaderProperties mySqlBinlogCdcPipelineReaderProperties) {
    return new MySqlBinaryLogClient(mySqlBinlogCdcPipelineReaderProperties.getCdcDbUserName(),
            mySqlBinlogCdcPipelineReaderProperties.getCdcDbPassword(),
            mySqlBinlogCdcPipelineReaderProperties.getDataSourceUrl(),
            createDataSource(mySqlBinlogCdcPipelineReaderProperties),
            mySqlBinlogCdcPipelineReaderProperties.getBinlogClientId(),
            mySqlBinlogCdcPipelineReaderProperties.getMySqlBinLogClientName(),
            mySqlBinlogCdcPipelineReaderProperties.getBinlogConnectionTimeoutInMilliseconds(),
            mySqlBinlogCdcPipelineReaderProperties.getMaxAttemptsForBinlogConnection(),
            curatorFramework,
            mySqlBinlogCdcPipelineReaderProperties.getLeadershipLockPath());
  }
}
