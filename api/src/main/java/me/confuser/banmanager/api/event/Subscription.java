package me.confuser.banmanager.api.event;

/**
 * Handle to a registered event subscription. Calling {@link #unsubscribe()}
 * removes the listener; subsequent calls are no-ops.
 *
 * <p>Plugins should keep their subscriptions in a list and unsubscribe on
 * shutdown / reload to avoid retaining classloaders.</p>
 */
public interface Subscription {

  /**
   * @return {@code true} once {@link #unsubscribe()} has been called
   */
  boolean isCancelled();

  /**
   * Detach this listener from the event bus.
   */
  void unsubscribe();
}
