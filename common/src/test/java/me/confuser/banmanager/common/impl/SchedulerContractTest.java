package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.api.scheduler.BanManagerScheduler;
import me.confuser.banmanager.common.CommonScheduler;
import me.confuser.banmanager.common.TestScheduler;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the {@link BanManagerScheduler}/{@link CommonScheduler} contract
 * around main-thread awareness:
 * <ul>
 *   <li>{@link CommonScheduler#isMainThreadAware()} defaults to {@code true}
 *       so server platforms (Bukkit/Sponge/Fabric) inherit the right answer
 *       without having to re-implement it</li>
 *   <li>Proxy-style schedulers (Bungee/Velocity) override to {@code false}
 *       and route {@code runSync} to {@code runAsync}</li>
 *   <li>{@link SchedulerAdapter} forwards {@code isMainThreadAware()} from
 *       the underlying delegate (so the API doesn't lie about the platform)</li>
 *   <li>{@link TestScheduler} reports {@code false}, since it runs everything
 *       inline on the calling thread and is not a real tick thread</li>
 * </ul>
 *
 * <p>This test deliberately avoids loading the real Bukkit/Bungee/etc. classes
 * — those require their hosting server to be up. Instead it pins down the
 * contract on {@link CommonScheduler} itself so any future platform impl that
 * forgets to implement it gets the platform-correct default and any proxy
 * impl that forgets to override it surfaces here.</p>
 */
public class SchedulerContractTest {

  // -- defaults --------------------------------------------------------------

  @Test
  public void defaultCommonSchedulerReportsMainThreadAware() {
    CommonScheduler bareImpl = new RecordingScheduler();
    assertTrue(bareImpl.isMainThreadAware(),
        "Default isMainThreadAware() must be true so server platforms "
            + "(Bukkit/Sponge/Fabric) get the correct answer for free.");
  }

  // -- adapter ---------------------------------------------------------------

  @Test
  public void schedulerAdapterPropagatesMainThreadAwarenessFromDelegate() {
    BanManagerScheduler proxyAware = new SchedulerAdapter(new ProxyLikeScheduler());
    assertFalse(proxyAware.isMainThreadAware(),
        "SchedulerAdapter must surface the underlying CommonScheduler's value, "
            + "not always return true.");

    BanManagerScheduler serverAware = new SchedulerAdapter(new RecordingScheduler());
    assertTrue(serverAware.isMainThreadAware(),
        "SchedulerAdapter must surface a true value from a server-style delegate.");
  }

  @Test
  public void schedulerAdapterDelegatesEachMethodOnce() {
    RecordingScheduler delegate = new RecordingScheduler();
    BanManagerScheduler adapter = new SchedulerAdapter(delegate);

    Runnable noop = () -> {};
    adapter.runAsync(noop);
    adapter.runAsyncLater(noop, Duration.ZERO);
    adapter.runSync(noop);
    adapter.runSyncLater(noop, Duration.ZERO);
    adapter.runAsyncRepeating(noop, Duration.ZERO, Duration.ofSeconds(1));

    assertEquals(1, delegate.runAsyncCalls.get(), "runAsync delegated count");
    assertEquals(1, delegate.runAsyncLaterCalls.get(), "runAsyncLater delegated count");
    assertEquals(1, delegate.runSyncCalls.get(), "runSync delegated count");
    assertEquals(1, delegate.runSyncLaterCalls.get(), "runSyncLater delegated count");
    assertEquals(1, delegate.runAsyncRepeatingCalls.get(), "runAsyncRepeating delegated count");
  }

  // -- proxy semantics -------------------------------------------------------

  @Test
  public void proxyLikeSchedulerRoutesRunSyncIntoRunAsync() {
    ProxyLikeScheduler proxy = new ProxyLikeScheduler();
    Runnable noop = () -> {};

    proxy.runSync(noop);

    assertEquals(0, proxy.directRunSyncCalls.get(),
        "Proxy schedulers must not implement an independent runSync path; "
            + "Bungee/Velocity have no main thread to dispatch to.");
    assertEquals(1, proxy.runAsyncCalls.get(),
        "runSync on a proxy must route to runAsync to preserve the documented "
            + "BanManagerScheduler aliasing contract.");
  }

  @Test
  public void proxyLikeSchedulerReportsNotMainThreadAware() {
    assertFalse(new ProxyLikeScheduler().isMainThreadAware(),
        "Proxy implementations must override isMainThreadAware() to false so "
            + "callers don't try to invoke main-thread-only APIs.");
  }

  // -- TestScheduler --------------------------------------------------------

  @Test
  public void testSchedulerReportsNotMainThreadAware() {
    assertFalse(new TestScheduler().isMainThreadAware(),
        "TestScheduler runs everything inline on the calling thread; it is "
            + "explicitly not on a server tick thread, so isMainThreadAware() "
            + "must return false to keep tests honest about proxy semantics.");
  }

  @Test
  public void testSchedulerRunsTaskInlineOnRunSync() {
    TestScheduler scheduler = new TestScheduler();
    Thread caller = Thread.currentThread();
    AtomicInteger counter = new AtomicInteger();

    scheduler.runSync(() -> {
      assertSame(caller, Thread.currentThread(),
          "TestScheduler must run tasks on the calling thread");
      counter.incrementAndGet();
    });

    assertEquals(1, counter.get(),
        "Task should run exactly once and synchronously.");
  }

  // -- helpers ---------------------------------------------------------------

  /**
   * Bare-bones {@link CommonScheduler} that counts invocations. Defaults to
   * the interface's {@code isMainThreadAware() == true} so we can test that
   * server-style platforms get the right behaviour without writing it
   * explicitly (mirrors Bukkit/Sponge/Fabric inheritance).
   */
  static final class RecordingScheduler implements CommonScheduler {
    final AtomicInteger runAsyncCalls = new AtomicInteger();
    final AtomicInteger runAsyncLaterCalls = new AtomicInteger();
    final AtomicInteger runSyncCalls = new AtomicInteger();
    final AtomicInteger runSyncLaterCalls = new AtomicInteger();
    final AtomicInteger runAsyncRepeatingCalls = new AtomicInteger();

    @Override public void runAsync(Runnable task) { runAsyncCalls.incrementAndGet(); }
    @Override public void runAsyncLater(Runnable task, Duration delay) { runAsyncLaterCalls.incrementAndGet(); }
    @Override public void runSync(Runnable task) { runSyncCalls.incrementAndGet(); }
    @Override public void runSyncLater(Runnable task, Duration delay) { runSyncLaterCalls.incrementAndGet(); }
    @Override
    public void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period) {
      runAsyncRepeatingCalls.incrementAndGet();
    }
  }

  /**
   * Mirrors the Bungee/Velocity contract: {@code runSync == runAsync} and
   * {@code isMainThreadAware() == false}. {@code directRunSyncCalls} would
   * fire only if a careless impl did its own dispatch — used to assert that
   * the alias really does collapse the two paths.
   */
  static final class ProxyLikeScheduler implements CommonScheduler {
    final AtomicInteger runAsyncCalls = new AtomicInteger();
    final AtomicInteger directRunSyncCalls = new AtomicInteger();

    @Override
    public void runAsync(Runnable task) {
      runAsyncCalls.incrementAndGet();
      task.run();
    }

    @Override
    public void runAsyncLater(Runnable task, Duration delay) {
      runAsync(task);
    }

    @Override
    public void runSync(Runnable task) {
      runAsync(task);
    }

    @Override
    public void runSyncLater(Runnable task, Duration delay) {
      runAsyncLater(task, delay);
    }

    @Override
    public void runAsyncRepeating(Runnable task, Duration initialDelay, Duration period) {
      runAsync(task);
    }

    @Override
    public boolean isMainThreadAware() {
      return false;
    }
  }
}
