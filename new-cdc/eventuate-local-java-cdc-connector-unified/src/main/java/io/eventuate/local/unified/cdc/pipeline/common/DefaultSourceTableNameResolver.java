package io.eventuate.local.unified.cdc.pipeline.common;

public interface DefaultSourceTableNameResolver {
  String resolve(String pipelineType);
}
