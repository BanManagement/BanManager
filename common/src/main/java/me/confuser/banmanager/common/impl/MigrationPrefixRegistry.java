package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.api.exception.BanManagerException;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which {@link ClassLoader} most recently registered each migration
 * {@code prefix}. Used by {@link MigrationServiceImpl} to reject silent
 * cross-plugin {@code bm_schema_version} collisions.
 *
 * <p>Holds a {@link WeakReference} to each classloader so a plugin reload
 * (Bukkit {@code /reload}, Velocity hot-reload) that drops the previous
 * loader frees the slot for the redeploy. A re-run from the same instance
 * is a no-op (idempotent migrations).</p>
 *
 * <p>Extracted from the service so the collision logic can be unit-tested
 * in isolation, without bringing up a {@code BanManagerPlugin}.</p>
 */
final class MigrationPrefixRegistry {

  private final ConcurrentHashMap<String, WeakReference<ClassLoader>> entries = new ConcurrentHashMap<>();

  /**
   * Claim {@code prefix} for {@code caller}. Idempotent for the same
   * caller; replaces a stale entry whose classloader has been GC'd.
   *
   * @throws BanManagerException when {@code prefix} is held by a different,
   *                             still-live classloader
   */
  void claim(String prefix, ClassLoader caller) {
    while (true) {
      WeakReference<ClassLoader> existing = entries.get(prefix);
      ClassLoader prior = existing != null ? existing.get() : null;

      if (prior == caller) return;

      if (prior != null) {
        throw new BanManagerException(
            "Migration prefix '" + prefix + "' is already registered by another plugin"
                + " (classloader " + prior + "). Choose a unique prefix to avoid"
                + " bm_schema_version row collisions.");
      }

      WeakReference<ClassLoader> next = new WeakReference<>(caller);
      if (existing == null) {
        if (entries.putIfAbsent(prefix, next) == null) return;
      } else if (entries.replace(prefix, existing, next)) {
        return;
      }
    }
  }
}
