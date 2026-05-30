package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.api.scheduler.BanManagerScheduler;
import me.confuser.banmanager.common.CommonScheduler;

import java.time.Duration;

/**
 * Thin adapter from the platform-specific {@link CommonScheduler} to the
 * public {@link BanManagerScheduler} API. The two interfaces are
 * intentionally identical at the moment so the adapter is a straight-through
 * delegate; it exists so platform schedulers stay free to add internal
 * methods without leaking them into the API surface.
 */
public final class SchedulerAdapter implements BanManagerScheduler {

  private final CommonScheduler delegate;

  public SchedulerAdapter(CommonScheduler delegate) {
    this.delegate = delegate;
  }

  @Override
  public void runAsync(Runnable task) {
    delegate.runAsync(task);
  }

  @Override
  public void runAsyncLater(Runnable task, Duration delay) {
    delegate.runAsyncLater(task, delay);
  }

  @Override
  public void runSync(Runnable task) {
    delegate.runSync(task);
  }

  @Override
  public void runSyncLater(Runnable task, Duration delay) {
    delegate.runSyncLater(task, delay);
  }

  @Override
  public void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period) {
    delegate.runAsyncRepeating(task, initialDelay, period);
  }

  @Override
  public boolean isMainThreadAware() {
    return delegate.isMainThreadAware();
  }
}
