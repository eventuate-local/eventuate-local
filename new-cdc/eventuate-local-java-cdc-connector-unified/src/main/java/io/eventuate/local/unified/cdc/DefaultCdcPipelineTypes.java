package io.eventuate.local.unified.cdc;

public interface DefaultCdcPipelineTypes {
  String mySqlBinlogPipelineType();
  String eventPollingPipelineType();
  String postgresWalPipelineType();
}
