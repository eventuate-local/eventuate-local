package io.eventuate.local.java.jdbckafkastore;

import io.eventuate.DuplicateTriggeringEventException;
import io.eventuate.EventContext;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.javaclient.spring.jdbc.EventAndTrigger;
import io.eventuate.javaclient.spring.jdbc.LoadedSnapshot;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SnapshotTriggeringEvents {

  private Map<String, Map<Integer, Long>> topicsToPartitionsAndOffsets = new HashMap<>();

  public Map<String, Map<Integer, Long>> getTopicsToPartitionsAndOffsets() {
    return topicsToPartitionsAndOffsets;
  }

  public void setTopicsToPartitionsAndOffsets(Map<String, Map<Integer, Long>> topicsToPartitionsAndOffsets) {
    this.topicsToPartitionsAndOffsets = topicsToPartitionsAndOffsets;
  }

  public void checkForDuplicateEvent(DecodedEtopContext etpo) {
    Map<Integer, Long> pos = topicsToPartitionsAndOffsets.get(etpo.topic);
    if (pos == null)
      return;
    Long maxOffset = pos.get(etpo.partition);
    if (maxOffset == null)
      return;

    if (etpo.offset <= maxOffset) {
      throw new DuplicateTriggeringEventException();
    }

  }

  public void add(String triggeringEvent) {
    DecodedEtopContext etpo = EtopEventContext.decode(triggeringEvent).get();

    Map<Integer, Long> pos = topicsToPartitionsAndOffsets.get(etpo.topic);
    if (pos == null) {
      topicsToPartitionsAndOffsets.put(etpo.topic, new HashMap<>(Collections.singletonMap(etpo.partition, etpo.offset)));
    } else {
      Long maxOffset = pos.get(etpo.partition);
      if (maxOffset == null || etpo.offset > maxOffset) {
        pos.put(etpo.partition, etpo.offset);
      }
    }
  }

  public boolean isEmpty() {
    return topicsToPartitionsAndOffsets.isEmpty();
  }

  public static void checkSnapshotForDuplicateEvent(LoadedSnapshot ss, EventContext te) {
    if (ss.getTriggeringEvents() == null)
      return;

    EtopEventContext.decode(te).ifPresent(etpo -> {
      JSonMapper.fromJson(ss.getTriggeringEvents(), SnapshotTriggeringEvents.class).checkForDuplicateEvent(etpo);

    });
  }

  public static String snapshotTriggeringEvents(Optional<LoadedSnapshot> previousSnapshot, List<EventAndTrigger> events, Optional<EventContext> eventContext) {
    return JSonMapper.toJson(getSnapshotTriggeringEvents(previousSnapshot, events, eventContext));
  }

  public static SnapshotTriggeringEvents getSnapshotTriggeringEvents(Optional<LoadedSnapshot> previousSnapshot, List<EventAndTrigger> events, Optional<EventContext> eventContext) {
    SnapshotTriggeringEvents ste = previousSnapshot.map(ss -> JSonMapper.fromJson(ss.getTriggeringEvents(), SnapshotTriggeringEvents.class))
            .orElseGet(SnapshotTriggeringEvents::new);
    events.stream()
            .filter(e -> e.triggeringEvent != null && EtopEventContext.isEtpoEvent(e.triggeringEvent))
            .forEach(e -> ste.add(e.triggeringEvent));

    eventContext.ifPresent(ec -> {
      if (EtopEventContext.isEtpoEvent(ec.getEventToken()))
        ste.add(ec.getEventToken());
    });
    return ste;
  }


}
