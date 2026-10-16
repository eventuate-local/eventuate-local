package io.eventuate;

import io.eventuate.RegisteredSubscription;

import java.util.LinkedList;
import java.util.List;

public class SubscriptionsRegistry {

  public List<RegisteredSubscription> getRegisteredSubscriptions() {
    return registeredSubscriptions;
  }

  private List<RegisteredSubscription> registeredSubscriptions = new LinkedList<>();

  public void add(RegisteredSubscription registeredSubscription) {
    registeredSubscriptions.add(registeredSubscription);
  }


}
