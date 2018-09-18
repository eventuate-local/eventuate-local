package io.eventuate.local.unified.cdc.pipeline.common;

import io.eventuate.local.common.BinlogEntryReader;

import java.util.HashMap;
import java.util.Map;

public class BinlogEntryReaderProvider {

  private Map<String, BinlogEntryReader> clients = new HashMap<>();

  public void addReader(String name, BinlogEntryReader reader) {
    clients.put(name, reader);
  }

  public <CLIENT> CLIENT getReader(String name) {
    return (CLIENT) clients.get(name);
  }

  public void start() {
    clients.values().forEach(BinlogEntryReader::start);
  }

  public void stop() {
    clients.values().forEach(BinlogEntryReader::stop);
  }
}
