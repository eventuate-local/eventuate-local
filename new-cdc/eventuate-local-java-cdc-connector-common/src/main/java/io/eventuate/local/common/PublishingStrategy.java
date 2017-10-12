package io.eventuate.local.common;

import java.util.Optional;

public interface PublishingStrategy<M> {

  String partitionKeyFor(M publishedEvent);

  String topicFor(M publishedEvent);

  String toJson(M eventInfo);

  Optional<Long> getCreateTime(M publishedEvent);
}
