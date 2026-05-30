package me.confuser.banmanager.api.scheduler;

import java.time.Duration;

/**
 * Platform-agnostic scheduler facade for fire-and-forget tasks (e.g. UI
 * updates, periodic cleanups, webhook fan-out).
 *
 * <p><b>Do not use for blocking JDBC.</b> All BanManager database operations
 * already return {@link java.util.concurrent.CompletableFuture} and run on a
 * dedicated DB-I/O executor. Submitting blocking work via {@link #runAsync}
 * on Sponge or Fabric runs on the platform's CPU-bound {@code ForkJoinPool}
 * and can starve other plugins.</p>
 *
 * <h2>Per-platform thread targets</h2>
 * <p>Every method in this interface is <strong>asynchronous submission</strong>
 * — the call returns immediately and the task runs at some later point on
 * the target executor. None of these methods ever execute the runnable
 * inline on the calling thread.</p>
 *
 * <table>
 *   <caption>Target thread per method, per platform</caption>
 *   <tr><th>Platform</th><th>{@code runAsync*}</th><th>{@code runSync*}</th><th>{@link #isMainThreadAware()}</th></tr>
 *   <tr><td>Bukkit</td><td>BanManager async pool</td><td>Bukkit main tick thread</td><td>{@code true}</td></tr>
 *   <tr><td>Sponge</td><td>BanManager async pool</td><td>Sponge main game-tick thread (via {@code Sponge.server().scheduler()})</td><td>{@code true}</td></tr>
 *   <tr><td>Fabric</td><td>BanManager async pool</td><td>{@code MinecraftServer.execute(...)} — server tick thread</td><td>{@code true}</td></tr>
 *   <tr><td>BungeeCord</td><td>Bungee scheduler async pool</td><td>same as {@code runAsync} (no main thread exists)</td><td>{@code false}</td></tr>
 *   <tr><td>Velocity</td><td>Velocity scheduler async pool</td><td>same as {@code runAsync} (no main thread exists)</td><td>{@code false}</td></tr>
 * </table>
 *
 * <h2>Picking the right method</h2>
 * <p>If your task touches a platform API that requires the server tick
 * thread (e.g. Bukkit {@code World}, entities, inventories), gate on
 * {@link #isMainThreadAware()} so it doesn't silently misbehave on a
 * proxy where {@code runSync} is an alias for {@code runAsync}:</p>
 *
 * <pre>{@code
 * if (scheduler.isMainThreadAware()) {
 *     scheduler.runSync(() -> world.spawnEntity(loc, EntityType.PIG));
 * } else {
 *     // proxy environment — there is no World here at all
 * }
 * }</pre>
 *
 * <p>For everything else (HTTP webhooks, cache warmups, periodic cleanups),
 * use {@link #runAsync} unconditionally — it works the same on every
 * platform.</p>
 */
public interface BanManagerScheduler {

  /**
   * Submit {@code task} to BanManager's async pool (server platforms) or
   * the platform scheduler's async pool (proxies). Returns immediately;
   * {@code task} runs at some later point on a worker thread.
   */
  void runAsync(Runnable task);

  /**
   * As {@link #runAsync(Runnable)} but submission is delayed by
   * {@code delay} before the task is dispatched to the async pool.
   */
  void runAsyncLater(Runnable task, Duration delay);

  /**
   * Schedule {@code task} on the platform's main thread when one exists
   * (Bukkit/Sponge/Fabric), or the worker pool otherwise (Bungee/Velocity).
   * See {@link #isMainThreadAware()} to discriminate at runtime.
   *
   * <p>This is always asynchronous submission — even when called from the
   * main thread, {@code task} is queued and runs at the next opportunity
   * rather than executing inline.</p>
   */
  void runSync(Runnable task);

  /**
   * Schedule {@code task} on the platform's main thread after {@code delay},
   * subject to the same main-thread caveats as {@link #runSync(Runnable)}.
   */
  void runSyncLater(Runnable task, Duration delay);

  /**
   * Submit {@code task} to the async pool, run after {@code initialDelay},
   * then re-run every {@code period} thereafter. Always async; never on the
   * main thread.
   */
  void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period);

  /**
   * @return {@code true} if {@link #runSync(Runnable)} actually pins the
   *         task to a server tick thread (Bukkit, Sponge, Fabric);
   *         {@code false} on proxies (BungeeCord, Velocity) where there is
   *         no main thread and {@link #runSync(Runnable)} is an alias for
   *         {@link #runAsync(Runnable)}.
   */
  boolean isMainThreadAware();
}
