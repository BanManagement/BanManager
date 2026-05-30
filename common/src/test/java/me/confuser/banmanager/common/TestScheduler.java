package me.confuser.banmanager.common;

import java.time.Duration;

public class TestScheduler implements CommonScheduler {

  @Override
  public void runAsync(Runnable task) {
    task.run();
  }

  @Override
  public void runAsyncLater(Runnable task, Duration delay) {
    task.run();
  }

  @Override
  public void runSync(Runnable task) {
    task.run();
  }

  @Override
  public void runSyncLater(Runnable task, Duration delay) {
    task.run();
  }

  @Override
  public void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period) {
    // In tests, just run the task once immediately
    task.run();
  }

  /**
   * Tests run every task on the calling thread, so by definition
   * {@link #runSync(Runnable)} executes "wherever the caller already is" —
   * not a real game tick thread. Reporting {@code false} keeps tests honest
   * about that and matches the proxy semantics that production code may
   * branch on.
   */
  @Override
  public boolean isMainThreadAware() {
    return false;
  }
}
