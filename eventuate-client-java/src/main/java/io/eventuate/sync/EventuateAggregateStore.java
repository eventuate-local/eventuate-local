package io.eventuate.sync;

import io.eventuate.*;
import io.eventuate.common.id.Int128;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * The synchronous-style interface to the event store
 */
public interface EventuateAggregateStore extends EventuateAggregateStoreEvents, EventuateAggregateStoreCrud {

}
