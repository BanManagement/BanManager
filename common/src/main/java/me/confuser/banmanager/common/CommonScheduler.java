package me.confuser.banmanager.common;

public interface CommonScheduler {
  void runAsync(Runnable task);
  void runAsyncLater(Runnable task, long delay);
  void runSync(Runnable task);
  void runSyncLater(Runnable task, long delay);
}
