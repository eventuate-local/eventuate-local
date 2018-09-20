package io.eventuate.local.unified.cdc.pipeline.common;

import io.eventuate.local.common.BinlogEntryReader;
import io.eventuate.local.unified.cdc.pipeline.common.properties.CdcPipelineReaderProperties;

import java.util.HashMap;
import java.util.Map;

public class BinlogEntryReaderProvider {

  private Map<String, BinlogEntryReader> clients = new HashMap<>();
  private Map<String, String> readerTypeByName = new HashMap<>();

  public void addReader(String name, String type, BinlogEntryReader reader) {
    clients.put(name, reader);
    readerTypeByName.put(name, type);
  }

  public <CLIENT> CLIENT getReader(String name) {
    return (CLIENT) clients.get(name);
  }

  public String getReaderType(String name) {
    return readerTypeByName.get(name);
  }

  public void start() {
    clients.values().forEach(BinlogEntryReader::start);
  }

  public void stop() {
    clients.values().forEach(BinlogEntryReader::stop);
  }
}
