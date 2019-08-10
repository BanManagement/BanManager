package me.confuser.banmanager.common;

public interface CommonScheduler {
  void runAsync(Runnable task);
  void runSync(Runnable task);
}
