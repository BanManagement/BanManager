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
}
