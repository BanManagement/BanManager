package me.confuser.banmanager.api.event;

/**
 * Subscription priority. Lower values run first.
 *
 * <p>Within a priority bucket, subscriptions run in registration order.</p>
 *
 * @see EventBus#subscribe(Class, EventPriority, java.util.function.Consumer)
 */
public enum EventPriority {
  LOWEST,
  LOW,
  NORMAL,
  HIGH,
  HIGHEST,
  /**
   * Runs after all other priorities. Use sparingly: handlers at this level
   * see the final state of mutable pre-events and so cannot be overridden.
   */
  MONITOR
}
