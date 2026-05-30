package me.confuser.banmanager.api;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Static locator for the platform's {@link BanManagerService} instance.
 *
 * <p>This works on every platform (Bukkit, Bungee, Velocity, Sponge, Fabric)
 * because the BanManager bootstrap calls {@link #set(BanManagerService)}
 * during plugin enable, so {@code BanManager.get()} is the portable
 * resolution path. On Bukkit you may alternatively use
 * {@code Bukkit.getServicesManager().load(BanManagerService.class)} — the
 * other platforms have no plugin-extensible service manager.</p>
 *
 * <h2>Resolution</h2>
 * <ol>
 *   <li>If {@link #set(BanManagerService)} has been called, the registered
 *       instance is returned. BanManager publishes itself this way on every
 *       platform during plugin enable.</li>
 *   <li>Otherwise {@link IllegalStateException} is thrown — there is no
 *       {@link java.util.ServiceLoader} fallback. {@code META-INF/services}
 *       discovery would only resolve consumer-side stubs, never the running
 *       plugin (whose classloader is invisible to consumer plugins), so it
 *       was a footgun and is intentionally absent.</li>
 * </ol>
 *
 * <h2>Tests</h2>
 * Unit tests should construct a service implementation (or test double)
 * directly and call {@link #set(BanManagerService)} in {@code @BeforeEach},
 * then {@link #clear()} in {@code @AfterEach}.
 */
public final class BanManager {

  private static final AtomicReference<BanManagerService> INSTANCE = new AtomicReference<>();

  private BanManager() {}

  /**
   * @return the active service instance
   * @throws IllegalStateException when BanManager has not finished enabling
   *                               yet, or when running outside a BanManager
   *                               environment
   */
  public static BanManagerService get() {
    BanManagerService current = INSTANCE.get();
    if (current != null) return current;
    throw new IllegalStateException(
        "BanManagerService has not been registered. Either BanManager hasn't enabled yet"
            + " or you're running outside a server with the BanManager plugin installed.");
  }

  /**
   * Register the active service. Called once by the platform plugin on
   * enable. {@code /bmreload} mutates the existing service in place rather
   * than re-publishing — consumer plugins should subscribe to
   * {@link me.confuser.banmanager.api.event.player.PluginReloadedEvent} to
   * re-register listeners after a reload, not poll {@link #get()} for a
   * fresh reference. Calls during a normal disable/enable cycle replace the
   * previous instance.
   */
  public static void set(BanManagerService service) {
    INSTANCE.set(service);
  }

  /**
   * Clear the registered service. Called on plugin disable to avoid
   * retaining classloaders.
   */
  public static void clear() {
    INSTANCE.set(null);
  }

  /**
   * @return {@code true} when {@link #get()} would succeed
   */
  public static boolean isAvailable() {
    return INSTANCE.get() != null;
  }
}
