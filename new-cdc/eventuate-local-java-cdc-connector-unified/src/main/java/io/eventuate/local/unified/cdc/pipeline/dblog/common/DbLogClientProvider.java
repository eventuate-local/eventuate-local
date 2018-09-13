package io.eventuate.local.unified.cdc.pipeline.dblog.common;

import io.eventuate.local.db.log.common.DbLogClient;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class DbLogClientProvider {

  private Map<ClientKey, DbLogClient> clients = new HashMap<>();

  public <CLIENT extends DbLogClient> CLIENT getOrCreateClient(String url, String user, String password, Supplier<CLIENT> clientSupplier) {
    ClientKey clientKey = new ClientKey(url, user, password);

    if (clients.containsKey(clientKey)) {
      return (CLIENT)clients.get(clientKey);
    }

    CLIENT client = clientSupplier.get();

    clients.put(clientKey, client);

    return client;
  }

  public void start() {
    clients.values().forEach(DbLogClient::start);
  }

  public void stop() {
    clients.values().forEach(DbLogClient::stop);
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
