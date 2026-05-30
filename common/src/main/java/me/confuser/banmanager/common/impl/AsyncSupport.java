package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.api.exception.BanManagerException;
import me.confuser.banmanager.api.exception.OperationCancelledException;
import me.confuser.banmanager.api.exception.StorageException;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Bridge between the legacy storage layer (which throws {@link SQLException})
 * and the public API surface (which returns {@link CompletableFuture} or
 * throws {@link BanManagerException}).
 *
 * <p>The dedicated DB-I/O executor passed in is sized to the Hikari pool by
 * {@link me.confuser.banmanager.common.impl.BanManagerServiceImpl}; that keeps
 * blocking JDBC off the platform's main scheduler and prevents the common
 * ForkJoinPool from being starved by webhook bursts.</p>
 */
public final class AsyncSupport {

  private final Executor executor;

  public AsyncSupport(Executor executor) {
    this.executor = executor;
  }

  /**
   * Run {@code callable} on the DB executor, returning its result via
   * {@link CompletableFuture}. {@link SQLException} is wrapped in
   * {@link StorageException}; other throwables propagate as-is so callers can
   * catch them or surface them via {@link CompletableFuture#exceptionally}.
   */
  public <T> CompletableFuture<T> async(SqlCallable<T> callable) {
    return CompletableFuture.supplyAsync(() -> sync(callable), executor);
  }

  /**
   * Run {@code callable} on the DB executor, returning {@link Void}.
   */
  public CompletableFuture<Void> asyncVoid(SqlRunnable runnable) {
    return CompletableFuture.runAsync(() -> syncVoid(runnable), executor);
  }

  /**
   * Bridge for create-style operations whose {@code *Sync()} variant throws
   * {@link OperationCancelledException} when a pre-event handler vetoes the
   * action. Async callers expect a sentinel value (typically
   * {@link java.util.Optional#empty()} or {@code Boolean.FALSE}) rather than
   * a failed future.
   *
   * <p>Any other thrown exception (including {@link SQLException} via
   * {@link #sync}) propagates through the future as usual.</p>
   *
   * @param callable the synchronous operation
   * @param onCancel sentinel returned when the operation is cancelled
   */
  public <T> CompletableFuture<T> asyncCancellable(SqlCallable<T> callable, T onCancel) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return sync(callable);
      } catch (OperationCancelledException ignored) {
        return onCancel;
      }
    }, executor);
  }

  /**
   * Synchronous execution helper. Use from {@code *Sync()} variants on the
   * service interfaces — callers are expected to have already moved to a
   * non-blocking thread.
   */
  public static <T> T sync(SqlCallable<T> callable) {
    return sync(callable, "BanManager storage operation failed");
  }

  /**
   * Context-rich sync variant. The supplied {@code contextMessage} is used
   * when wrapping {@link SQLException} into {@link StorageException} and any
   * other checked exception into {@link BanManagerException}, so debug logs
   * and crash reports name the operation that actually failed (e.g.
   * {@code "Failed to delete report 12"}). Use this from {@code *Sync()}
   * service variants where the generic default message would lose useful
   * context.
   */
  public static <T> T sync(SqlCallable<T> callable, String contextMessage) {
    try {
      return callable.call();
    } catch (SQLException e) {
      throw new StorageException(contextMessage, e);
    } catch (BanManagerException e) {
      throw e;
    } catch (Exception e) {
      throw new BanManagerException(contextMessage, e);
    }
  }

  public static void syncVoid(SqlRunnable runnable) {
    syncVoid(runnable, "BanManager storage operation failed");
  }

  /**
   * Context-rich {@link #syncVoid(SqlRunnable)} variant. See
   * {@link #sync(SqlCallable, String)} for rationale.
   */
  public static void syncVoid(SqlRunnable runnable, String contextMessage) {
    try {
      runnable.run();
    } catch (SQLException e) {
      throw new StorageException(contextMessage, e);
    } catch (BanManagerException e) {
      throw e;
    } catch (Exception e) {
      throw new BanManagerException(contextMessage, e);
    }
  }

  /**
   * @return the underlying DB-I/O {@link Executor}. Exposed for
   *         service implementations that need to chain additional
   *         {@code thenApplyAsync}-style stages onto the same pool rather
   *         than the {@link java.util.concurrent.ForkJoinPool#commonPool()
   *         common pool}.
   */
  public Executor executor() {
    return executor;
  }

  @FunctionalInterface
  public interface SqlCallable<T> {
    T call() throws Exception;
  }

  @FunctionalInterface
  public interface SqlRunnable {
    void run() throws Exception;
  }
}
