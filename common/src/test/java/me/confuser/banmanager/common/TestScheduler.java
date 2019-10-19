package me.confuser.banmanager.common;

public class TestScheduler implements CommonScheduler {

  @Override
  public void runAsync(Runnable task) {
    task.run();
  }

  @Override
  public void runAsyncLater(Runnable task, long delay) {
    task.run();
  }

  @Override
  public void runSync(Runnable task) {
    task.run();
  }

  @Override
  public void runSyncLater(Runnable task, long delay) {
    task.run();
  }
}
