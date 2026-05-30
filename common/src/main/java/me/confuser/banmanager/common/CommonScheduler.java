package me.confuser.banmanager.common;

import java.time.Duration;

public interface CommonScheduler {
  void runAsync(Runnable task);
  void runAsyncLater(Runnable task, Duration delay);
  void runSync(Runnable task);
  void runSyncLater(Runnable task, Duration delay);
  void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period);
  default void cancelAll() {}

  /**
   * @return {@code true} when {@link #runSync(Runnable)} pins to the server
   *         tick thread (Bukkit/Sponge/Fabric); {@code false} on proxies
   *         where it aliases to {@link #runAsync(Runnable)} (Bungee/Velocity).
   *         Mirrors
   *         {@link me.confuser.banmanager.api.scheduler.BanManagerScheduler#isMainThreadAware()}.
   */
  default boolean isMainThreadAware() {
    return true;
  }
}
