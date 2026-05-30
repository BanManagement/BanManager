package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.api.exception.BanManagerException;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behavioural contract for {@link MigrationPrefixRegistry}: rejects
 * different-classloader collisions, allows same-classloader idempotency,
 * and reclaims slots whose classloader has been GC'd.
 */
class MigrationPrefixRegistryTest {

  @Test
  void firstClaimSucceeds() {
    MigrationPrefixRegistry registry = new MigrationPrefixRegistry();
    assertDoesNotThrow(() -> registry.claim("plugin-a", new ClassLoader() {}));
  }

  @Test
  void sameClassloaderRepeatClaimIsNoop() {
    MigrationPrefixRegistry registry = new MigrationPrefixRegistry();
    ClassLoader cl = new ClassLoader() {};
    registry.claim("plugin-a", cl);
    assertDoesNotThrow(() -> registry.claim("plugin-a", cl));
    assertDoesNotThrow(() -> registry.claim("plugin-a", cl));
  }

  @Test
  void differentClassloaderSamePrefixRejected() {
    MigrationPrefixRegistry registry = new MigrationPrefixRegistry();
    ClassLoader first = new ClassLoader() {};
    ClassLoader second = new ClassLoader() {};
    registry.claim("plugin-a", first);

    BanManagerException ex = assertThrows(BanManagerException.class,
        () -> registry.claim("plugin-a", second));
    assertTrue(ex.getMessage().contains("plugin-a"),
        "Error must name the colliding prefix; got: " + ex.getMessage());
    assertTrue(ex.getMessage().contains("classloader"),
        "Error must mention classloader to point operators at the cause; got: " + ex.getMessage());
  }

  @Test
  void differentPrefixSameClassloaderAllowed() {
    MigrationPrefixRegistry registry = new MigrationPrefixRegistry();
    ClassLoader cl = new ClassLoader() {};
    assertDoesNotThrow(() -> registry.claim("plugin-a", cl));
    assertDoesNotThrow(() -> registry.claim("plugin-b", cl));
  }

  @Test
  void deadClassloaderReleasesSlot() throws Exception {
    MigrationPrefixRegistry registry = new MigrationPrefixRegistry();
    ClassLoader replacement = new ClassLoader() {};

    // Claim with a classloader that can be GC'd, then drop the strong
    // reference. We don't have direct control over GC, but we can force
    // it deterministically by allocating until the weak ref is cleared.
    WeakReference<ClassLoader> tombstone = registerAndDrop(registry, "plugin-a");
    forceWeakRefClear(tombstone);

    // Slot must now be reclaimable by a different classloader without
    // throwing — the previous holder is gone.
    assertDoesNotThrow(() -> registry.claim("plugin-a", replacement));
  }

  /**
   * Registers a freshly-allocated classloader against {@code prefix}, then
   * returns a weak reference to it so the test can drop the strong
   * reference and let the GC reclaim it. Kept in its own method so the
   * local variable goes out of scope cleanly.
   */
  private WeakReference<ClassLoader> registerAndDrop(MigrationPrefixRegistry registry, String prefix) {
    ClassLoader transientLoader = new ClassLoader() {};
    registry.claim(prefix, transientLoader);
    return new WeakReference<>(transientLoader);
  }

  /**
   * Spin {@link System#gc()} (with allocation pressure) until the
   * referent is cleared, or fail loud if the JVM refuses to collect after
   * a generous budget. Required for the dead-classloader path to be a
   * deterministic test rather than a flaky hope.
   */
  private void forceWeakRefClear(WeakReference<?> ref) {
    for (int i = 0; i < 50 && ref.get() != null; i++) {
      System.gc();
      // Allocate enough garbage to push the young gen into a collection.
      byte[] pressure = new byte[1024 * 1024];
      pressure[0] = (byte) i;
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
    }
    if (ref.get() != null) {
      throw new AssertionError("WeakReference was not cleared after repeated GC hints — "
          + "test cannot proceed deterministically on this JVM");
    }
  }
}
