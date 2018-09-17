package io.eventuate.local.unified.cdc.pipeline.common;

import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BinlogEntryReaderProvider {

  private Map<ClientKey, ClientValue> clients = new HashMap<>();

  public <CLIENT> CLIENT getOrCreateClient(String url,
                                           String user,
                                           String password,
                                           Supplier<CLIENT> clientSupplier,
                                           Consumer<CLIENT> startCallback,
                                           Consumer<CLIENT> stopCallback) {

    ClientKey clientKey = new ClientKey(url, user, password);

    if (clients.containsKey(clientKey)) {
      return (CLIENT)clients.get(clientKey);
    }

    CLIENT client = clientSupplier.get();

    clients.put(clientKey, new ClientValue(client, () -> startCallback.accept(client), () -> stopCallback.accept(client)));

    return client;
  }

  public void start() {
    clients.values().forEach(v -> v.startCallback.run());
  }

  public void stop() {
    clients.values().forEach(v -> v.stopCallback.run());
  }

  public static class ClientValue {
    private Object client;
    private Runnable startCallback;
    private Runnable stopCallback;

    public ClientValue(Object client, Runnable startCallback, Runnable stopCallback) {
      this.client = client;
      this.startCallback = startCallback;
      this.stopCallback = stopCallback;
    }

    public Object getClient() {
      return client;
    }

    public Runnable getStartCallback() {
      return startCallback;
    }

    public Runnable getStopCallback() {
      return stopCallback;
    }
  }

  private static class ClientKey {
    private String url;
    private String user;
    private String password;

    public ClientKey(String url, String user, String password) {
      this.url = url;
      this.user = user;
      this.password = password;
    }

    @Override
    public boolean equals(Object o) {
      return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
      return Objects.hash(url, user, password);
    }
  }
}
