package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.api.exception.BanManagerException;
import me.confuser.banmanager.api.exception.OperationCancelledException;
import me.confuser.banmanager.api.exception.StorageException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link AsyncSupport}'s exception bridging contract:
 * <ul>
 *   <li>{@link SQLException} is wrapped in {@link StorageException}</li>
 *   <li>{@link BanManagerException} subtypes propagate unchanged</li>
 *   <li>Other checked exceptions become {@link BanManagerException}</li>
 *   <li>{@code asyncCancellable} converts {@link OperationCancelledException}
 *       into a sentinel return value rather than a failed future</li>
 * </ul>
 */
public class AsyncSupportTest {

  private ExecutorService executor;
  private AsyncSupport async;

  @BeforeEach
  public void setUp() {
    executor = Executors.newSingleThreadExecutor();
    async = new AsyncSupport(executor);
  }

  @AfterEach
  public void tearDown() throws InterruptedException {
    executor.shutdown();
    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
      executor.shutdownNow();
    }
  }

  @Test
  public void syncReturnsCallableResult() {
    String result = AsyncSupport.sync(() -> "hello");
    assertEquals("hello", result);
  }

  @Test
  public void syncWrapsSqlException() {
    SQLException root = new SQLException("connection refused");
    StorageException thrown = assertThrows(StorageException.class,
        () -> AsyncSupport.sync(() -> { throw root; }));
    assertSame(root, thrown.getCause());
  }

  @Test
  public void syncPropagatesBanManagerExceptionUnchanged() {
    OperationCancelledException original = new OperationCancelledException("vetoed");
    OperationCancelledException thrown = assertThrows(OperationCancelledException.class,
        () -> AsyncSupport.sync(() -> { throw original; }));
    assertSame(original, thrown);
  }

  @Test
  public void syncPropagatesStorageExceptionUnchanged() {
    StorageException original = new StorageException("boom", new SQLException("driver"));
    StorageException thrown = assertThrows(StorageException.class,
        () -> AsyncSupport.sync(() -> { throw original; }));
    assertSame(original, thrown);
  }

  @Test
  public void syncWrapsOtherCheckedExceptionsAsBanManagerException() {
    IOException root = new IOException("disk gone");
    BanManagerException thrown = assertThrows(BanManagerException.class,
        () -> AsyncSupport.sync(() -> { throw root; }));
    assertSame(root, thrown.getCause());
  }

  @Test
  public void syncVoidExecutesRunnable() {
    AtomicBoolean executed = new AtomicBoolean();
    AsyncSupport.syncVoid(() -> executed.set(true));
    assertTrue(executed.get());
  }

  @Test
  public void syncVoidWrapsSqlException() {
    SQLException root = new SQLException("connection refused");
    StorageException thrown = assertThrows(StorageException.class,
        () -> AsyncSupport.syncVoid(() -> { throw root; }));
    assertSame(root, thrown.getCause());
  }

  @Test
  public void asyncCompletesWithResult() throws Exception {
    CompletableFuture<Integer> future = async.async(() -> 42);
    assertEquals(42, future.get(5, TimeUnit.SECONDS));
  }

  @Test
  public void asyncRunsOnSuppliedExecutor() throws Exception {
    CompletableFuture<Thread> future = async.async(Thread::currentThread);
    Thread runner = future.get(5, TimeUnit.SECONDS);
    assertFalse(runner == Thread.currentThread(),
        "callable should not run on the calling thread");
  }

  @Test
  public void asyncFailsFutureWithStorageExceptionForSqlException() {
    SQLException root = new SQLException("network");
    CompletableFuture<String> future = async.async(() -> { throw root; });

    ExecutionException ex = assertThrows(ExecutionException.class,
        () -> future.get(5, TimeUnit.SECONDS));
    assertTrue(ex.getCause() instanceof StorageException,
        "expected StorageException, got " + ex.getCause());
    assertSame(root, ex.getCause().getCause());
  }

  @Test
  public void asyncCancellableReturnsResultWhenCallableSucceeds() throws Exception {
    CompletableFuture<Optional<String>> future =
        async.asyncCancellable(() -> Optional.of("ok"), Optional.empty());

    assertEquals(Optional.of("ok"), future.get(5, TimeUnit.SECONDS));
  }

  @Test
  public void asyncCancellableReturnsSentinelWhenCancelled() throws Exception {
    CompletableFuture<Optional<String>> future = async.asyncCancellable(() -> {
      throw new OperationCancelledException("vetoed");
    }, Optional.empty());

    assertEquals(Optional.empty(), future.get(5, TimeUnit.SECONDS));
    assertFalse(future.isCompletedExceptionally(),
        "asyncCancellable must not surface OperationCancelledException as a failed future");
  }

  @Test
  public void asyncCancellableSupportsBooleanSentinel() throws Exception {
    CompletableFuture<Boolean> future = async.asyncCancellable(() -> {
      throw new OperationCancelledException("vetoed");
    }, Boolean.FALSE);

    assertEquals(Boolean.FALSE, future.get(5, TimeUnit.SECONDS));
  }

  @Test
  public void asyncCancellablePropagatesStorageException() {
    SQLException root = new SQLException("boom");
    CompletableFuture<Optional<String>> future = async.asyncCancellable(() -> {
      throw root;
    }, Optional.empty());

    ExecutionException ex = assertThrows(ExecutionException.class,
        () -> future.get(5, TimeUnit.SECONDS));
    assertTrue(ex.getCause() instanceof StorageException);
    assertSame(root, ex.getCause().getCause());
  }

  @Test
  public void asyncCancellablePropagatesUnrelatedRuntimeException() {
    IllegalStateException root = new IllegalStateException("nope");
    CompletableFuture<Optional<String>> future = async.asyncCancellable(() -> {
      throw root;
    }, Optional.empty());

    ExecutionException ex = assertThrows(ExecutionException.class,
        () -> future.get(5, TimeUnit.SECONDS));
    assertTrue(ex.getCause() instanceof BanManagerException,
        "non-cancellation exceptions should still surface; got " + ex.getCause());
  }

  @Test
  public void asyncVoidCompletes() throws Exception {
    AtomicBoolean ran = new AtomicBoolean();
    async.asyncVoid(() -> ran.set(true)).get(5, TimeUnit.SECONDS);
    assertTrue(ran.get());
  }

  @Test
  public void executorAccessorReturnsConfiguredExecutor() {
    assertSame(executor, async.executor());
  }

  @Test
  public void syncWithContextMessageWrapsSqlExceptionWithThatMessage() {
    SQLException root = new SQLException("driver");
    StorageException thrown = assertThrows(StorageException.class,
        () -> AsyncSupport.sync(() -> { throw root; }, "Failed to delete report 12"));
    assertEquals("Failed to delete report 12", thrown.getMessage());
    assertSame(root, thrown.getCause());
  }

  @Test
  public void syncWithContextMessageWrapsOtherCheckedExceptionsWithThatMessage() {
    IOException root = new IOException("disk gone");
    BanManagerException thrown = assertThrows(BanManagerException.class,
        () -> AsyncSupport.sync(() -> { throw root; }, "Failed to load report states"));
    assertEquals("Failed to load report states", thrown.getMessage());
    assertSame(root, thrown.getCause());
  }

  @Test
  public void syncWithContextMessagePropagatesBanManagerExceptionUnchanged() {
    OperationCancelledException original = new OperationCancelledException("vetoed");
    OperationCancelledException thrown = assertThrows(OperationCancelledException.class,
        () -> AsyncSupport.sync(() -> { throw original; }, "Should not be used"));
    assertSame(original, thrown);
    assertEquals("vetoed", thrown.getMessage());
  }

  @Test
  public void syncVoidWithContextMessageWrapsSqlExceptionWithThatMessage() {
    SQLException root = new SQLException("driver");
    StorageException thrown = assertThrows(StorageException.class,
        () -> AsyncSupport.syncVoid(() -> { throw root; }, "Failed to mark warning 5 read"));
    assertEquals("Failed to mark warning 5 read", thrown.getMessage());
    assertSame(root, thrown.getCause());
  }

  @Test
  public void syncVoidWithContextMessagePropagatesBanManagerExceptionUnchanged() {
    StorageException original = new StorageException("original", new SQLException());
    StorageException thrown = assertThrows(StorageException.class,
        () -> AsyncSupport.syncVoid(() -> { throw original; }, "Should not be used"));
    assertSame(original, thrown);
  }

  @Test
  public void defaultMessageMatchesPreviousContract() {
    SQLException root = new SQLException("driver");
    StorageException thrown = assertThrows(StorageException.class,
        () -> AsyncSupport.sync(() -> { throw root; }));
    assertEquals("BanManager storage operation failed", thrown.getMessage());
  }
}
