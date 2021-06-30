package io.eventuate.example.banking.domain;


import io.eventuate.Event;
import io.eventuate.EventEntity;

@EventEntity(entity="io.eventuate.example.banking.domain.Account")
public interface AccountEvent extends Event {
}
