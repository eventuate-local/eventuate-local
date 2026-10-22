package io.eventuate;

import java.util.List;

public class CommandOutcome {
  public final List<Event> result;
  public final Throwable throwable;

  public CommandOutcome(List<Event> result) {
    this.result = result;
    this.throwable = null;
  }

  public CommandOutcome(Throwable throwable) {
    this.result = null;
    this.throwable = throwable;
  }

  public boolean isFailure() {
    return throwable != null;
  }


}
