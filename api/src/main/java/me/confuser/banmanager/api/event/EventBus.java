package me.confuser.banmanager.api.event;

import java.util.function.Consumer;

/**
 * Typed event bus. The single place where plugins subscribe to BanManager
 * events; replaces the v7 platform-specific event classes.
 *
 * <p>Pre-events carry mutable {@code *Request} payloads that handlers may
 * modify before the database write occurs; post-events carry immutable
 * record DTOs.</p>
 *
 * <h2>Threading model — IMPORTANT</h2>
 * <p>Dispatch is <strong>synchronous on the publishing thread</strong>,
 * preserving v7 {@code callEvent(...)} semantics. Because BanManager's
 * storage layer publishes events from its own dedicated DB-I/O executor,
 * subscribers run <strong>off</strong> the server tick thread by default.
 * Two consequences follow:</p>
 *
 * <ol>
 *   <li><b>Subscribers must return promptly.</b> A slow handler (target
 *       budget: under ~10&nbsp;ms steady-state, never any blocking I/O)
 *       holds up <em>every</em> subsequent ban / mute / warn write because
 *       it blocks the DB-I/O executor's worker. Offload heavy work to
 *       {@code BanManagerService#scheduler().runAsync(...)} and return.</li>
 *   <li><b>Subscribers may not call platform APIs that require the server
 *       tick thread</b> (Bukkit {@code World}, etc.) without first
 *       hopping back via {@code scheduler().runSync(...)} — and only on
 *       {@link me.confuser.banmanager.api.scheduler.BanManagerScheduler#isMainThreadAware()
 *       main-thread-aware} platforms.</li>
 * </ol>
 *
 * <p>Errors thrown by a subscriber are caught, logged with the handler's
 * class name, and do not abort dispatch; the next listener still fires.</p>
 *
 * <h2>Cancellation</h2>
 * <p>For {@link CancellableEvent} subclasses, calling
 * {@link CancellableEvent#cancel()} from a higher-priority handler stops
 * the operation. Lower-priority handlers do not see cancelled events
 * unless they registered with {@code ignoreCancelled = false} via
 * {@link #subscribe(Class, EventPriority, boolean, Consumer)}. See
 * {@link EventPriority} for ordering.</p>
 *
 * @see me.confuser.banmanager.api.event.player.PlayerBanEvent
 * @see me.confuser.banmanager.api.event.player.PlayerBannedEvent
 * @see me.confuser.banmanager.api.scheduler.BanManagerScheduler
 */
public interface EventBus {

  /**
   * Subscribe to {@code type} at {@link EventPriority#NORMAL} priority.
   *
   * @return a handle that can be used to {@link Subscription#unsubscribe()}
   */
  <E extends BanManagerEvent> Subscription subscribe(Class<E> type, Consumer<E> handler);

  /**
   * Subscribe to {@code type} at the requested priority.
   */
  <E extends BanManagerEvent> Subscription subscribe(Class<E> type, EventPriority priority, Consumer<E> handler);

  /**
   * Subscribe and additionally receive cancelled cancellable events. By
   * default, cancellable events are not redelivered to lower-priority
   * handlers once cancelled.
   */
  <E extends BanManagerEvent> Subscription subscribe(Class<E> type, EventPriority priority, boolean ignoreCancelled, Consumer<E> handler);

  /**
   * Publish an event. Returns the same instance for inspection (useful for
   * testing storage code paths that need to know whether the event was
   * cancelled).
   */
  <E extends BanManagerEvent> E publish(E event);
}
