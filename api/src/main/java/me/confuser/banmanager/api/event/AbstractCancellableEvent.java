package me.confuser.banmanager.api.event;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Convenience base class for {@link CancellableEvent} implementations.
 * Thread-safe so handlers running on different threads can read the
 * cancellation state.
 */
public abstract class AbstractCancellableEvent implements CancellableEvent {

  private final AtomicBoolean cancelled = new AtomicBoolean();

  @Override
  public boolean isCancelled() {
    return cancelled.get();
  }

  @Override
  public void cancel() {
    cancelled.set(true);
  }

  @Override
  public void uncancel() {
    cancelled.set(false);
  }
}
