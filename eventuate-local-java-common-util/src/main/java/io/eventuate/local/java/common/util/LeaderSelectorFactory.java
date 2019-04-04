package io.eventuate.local.java.common.util;

public interface LeaderSelectorFactory {
  CommonLeaderSelector create(String lockId, String leaderId, Runnable leaderSelectedCallback, Runnable leaderRemovedCallback);
}
