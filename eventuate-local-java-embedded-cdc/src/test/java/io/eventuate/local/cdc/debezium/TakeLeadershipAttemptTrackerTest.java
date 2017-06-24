package io.eventuate.local.cdc.debezium;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TakeLeadershipAttemptTrackerTest {

  class MyTakeLeadershipAttemptTracker extends TakeLeadershipAttemptTracker {

    private List<LocalDateTime> times;

    public MyTakeLeadershipAttemptTracker(int maxAttempts, long retryPeriodInMilliseconds, List<LocalDateTime> times) {
      super(maxAttempts, retryPeriodInMilliseconds);
      this.times = times;
    }

    @Override
    protected LocalDateTime now() {
      LocalDateTime x = times.get(0);
      times = times.subList(1, times.size());
      return x;
    }
  }

  @Test
  public void shouldAllowRetryAfterLongtime() {
    List<LocalDateTime> times = Arrays.asList(LocalDateTime.now(), LocalDateTime.now().plusSeconds(120));
    MyTakeLeadershipAttemptTracker t = new MyTakeLeadershipAttemptTracker(3, 60 * 1000, times);

    t.attempting();
    assertTrue(t.shouldAttempt());
  }

  @Test
  public void shouldRejectTooManyRetries() {
    LocalDateTime n = LocalDateTime.now();

    List<LocalDateTime> times =
            Arrays.asList(n,
                    n.plusSeconds(1),
                    n.plusSeconds(2),
                    n.plusSeconds(3),
                    n.plusSeconds(4),
                    n.plusSeconds(5),
                    n.plusSeconds(6),
                    n.plusSeconds(7),
                    n.plusSeconds(8),
                    n.plusSeconds(9)
                    );

    MyTakeLeadershipAttemptTracker t = new MyTakeLeadershipAttemptTracker(3, 60 * 1000, times);

    t.attempting();
    assertTrue(t.shouldAttempt());

    t.attempting();
    assertTrue(t.shouldAttempt());

    t.attempting();
    assertTrue(t.shouldAttempt());

    t.attempting();
    assertFalse(t.shouldAttempt());

  }

}