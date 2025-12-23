package me.confuser.banmanager.common;

import java.time.Duration;

public interface CommonScheduler {
  void runAsync(Runnable task);
  void runAsyncLater(Runnable task, Duration delay);
  void runSync(Runnable task);
  void runSyncLater(Runnable task, Duration delay);
  void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period);
}
