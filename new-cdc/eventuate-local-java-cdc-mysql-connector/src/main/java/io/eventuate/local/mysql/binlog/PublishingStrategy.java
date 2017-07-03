package io.eventuate.local.mysql.binlog;

import java.util.Optional;

public interface PublishingStrategy<M> {


  String partitionKeyFor(M publishedEvent);

  String topicFor(M publishedEvent);

  String toJson(M eventInfo);

  Optional<Long> getCreateTime(M publishedEvent);
}
