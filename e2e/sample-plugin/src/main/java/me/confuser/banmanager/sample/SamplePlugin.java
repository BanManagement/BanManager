package me.confuser.banmanager.sample;

import me.confuser.banmanager.api.BanManager;
import me.confuser.banmanager.api.BanManagerService;
import me.confuser.banmanager.api.database.DatabaseKind;
import me.confuser.banmanager.api.database.MigrationService.MigrationConfig;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.event.EventPriority;
import me.confuser.banmanager.api.event.Subscription;
import me.confuser.banmanager.api.event.player.PlayerBannedEvent;
import me.confuser.banmanager.api.event.player.PlayerDeniedEvent;
import me.confuser.banmanager.api.event.player.PluginReloadedEvent;
import me.confuser.banmanager.api.request.BanRequest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * End-to-end sample plugin that exercises every public surface of
 * {@link BanManagerService}. It is built and shipped as part of the BanManager
 * repository so the API artifact is verified by a real consumer plugin on
 * every CI run.
 *
 * <p>Demonstrates:</p>
 * <ul>
 *   <li>Service resolution via both {@link BanManager#get()} and Bukkit's
 *       {@code ServicesManager}.</li>
 *   <li>Subscribing to {@link PlayerBannedEvent}, {@link PlayerDeniedEvent}
 *       (placeholder injection) and {@link PluginReloadedEvent}
 *       (re-registration on {@code /bmreload}).</li>
 *   <li>Async writes via {@link CompletableFuture} and synchronous variants
 *       via the {@code *Sync} siblings.</li>
 *   <li>Running plugin-owned SQL migrations via
 *       {@link me.confuser.banmanager.api.database.MigrationService}.</li>
 *   <li>Direct {@link DataSource} access via
 *       {@link me.confuser.banmanager.api.database.DatabaseAccess} including
 *       table-name resolution.</li>
 *   <li>Cross-platform scheduling via
 *       {@link me.confuser.banmanager.api.scheduler.BanManagerScheduler}.</li>
 * </ul>
 */
public class SamplePlugin extends JavaPlugin {

  private final List<Subscription> subscriptions = new ArrayList<>();

  @Override
  public void onEnable() {
    if (!BanManager.isAvailable()) {
      getLogger().warning("BanManager is not enabled yet - aborting sample plugin");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    BanManagerService bm = BanManager.get();

    BanManagerService bukkitLookup = getServer().getServicesManager().load(BanManagerService.class);
    if (bukkitLookup != bm) {
      getLogger().warning("Bukkit ServicesManager and BanManager.get() returned different instances - this should never happen");
    }

    runMigrations(bm);
    registerEventSubscriptions(bm);
    schedulePeriodicCounts(bm);

    getLogger().info("Sample plugin ready - main-thread aware? " + bm.scheduler().isMainThreadAware());
  }

  @Override
  public void onDisable() {
    subscriptions.forEach(Subscription::unsubscribe);
    subscriptions.clear();
  }

  private void runMigrations(BanManagerService bm) {
    bm.migrations().run(new MigrationConfig(
        DatabaseKind.LOCAL,
        "sample",
        "db/sample",
        getClass().getClassLoader()));
  }

  private void registerEventSubscriptions(BanManagerService bm) {
    subscriptions.add(bm.events().subscribe(
        PlayerBannedEvent.class,
        EventPriority.MONITOR,
        event -> {
          if (event.silent()) return;
          String message = "[Sample] " + event.ban().player().name()
              + " was banned for " + event.ban().reason();
          bm.scheduler().runSync(() -> getServer().broadcastMessage(message));
        }));

    subscriptions.add(bm.events().subscribe(
        PlayerDeniedEvent.class,
        event -> {
          event.placeholders().put("appeal_url", "https://bans.example.com/appeal");
          recordDeniedLogin(bm, event);
        }));

    subscriptions.add(bm.events().subscribe(PluginReloadedEvent.class, event -> {
      getLogger().info("BanManager reloaded - rebinding subscriptions");
      onDisable();
      registerEventSubscriptions(bm);
    }));
  }

  private void recordDeniedLogin(BanManagerService bm, PlayerDeniedEvent event) {
    DataSource ds = bm.database().localDataSource();
    bm.scheduler().runAsync(() -> {
      try (Connection conn = ds.getConnection();
           PreparedStatement ps = conn.prepareStatement(
               "INSERT INTO bm_sample_denied_logins (uuid, name, reason, created) VALUES (?, ?, ?, ?)")) {
        ps.setBytes(1, event.uuid().map(SamplePlugin::uuidToBytes).orElse(null));
        ps.setString(2, event.name());
        ps.setString(3, event.reason().name());
        ps.setLong(4, System.currentTimeMillis() / 1000L);
        ps.executeUpdate();
      } catch (Exception ex) {
        getLogger().log(Level.WARNING, "Failed to record denied login", ex);
      }
    });
  }

  private void schedulePeriodicCounts(BanManagerService bm) {
    bm.scheduler().runAsyncRepeating(
        () -> printCounts(bm, getServer().getConsoleSender(), false),
        Duration.ofMinutes(5),
        Duration.ofHours(1));
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!BanManager.isAvailable()) {
      sender.sendMessage("BanManager is not available");
      return true;
    }
    BanManagerService bm = BanManager.get();
    return switch (command.getName().toLowerCase()) {
      case "sampleban" -> handleSampleBan(bm, sender, args);
      case "samplecount" -> {
        printCounts(bm, sender, true);
        yield true;
      }
      default -> false;
    };
  }

  private boolean handleSampleBan(BanManagerService bm, CommandSender sender, String[] args) {
    if (args.length < 1) {
      sender.sendMessage("Usage: /sampleban <player> [reason]");
      return true;
    }
    String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "Sample ban";

    CompletableFuture<Optional<Player>> playerFuture = bm.players().findByName(args[0]);

    playerFuture.thenCompose(maybe -> {
      if (maybe.isEmpty()) {
        bm.scheduler().runSync(() -> sender.sendMessage("Unknown player: " + args[0]));
        return CompletableFuture.completedFuture(Optional.empty());
      }
      Player target = maybe.get();
      Player actor = bm.players().console();
      return bm.bans().ban(new BanRequest(target.uuid(), actor.uuid(), reason));
    }).thenAccept(ban -> ban.ifPresentOrElse(
        b -> bm.scheduler().runSync(() ->
            sender.sendMessage("Banned " + b.player().name() + " for " + b.reason())),
        () -> bm.scheduler().runSync(() ->
            sender.sendMessage("Ban was cancelled by another plugin"))))
        .whenComplete((unused, ex) -> {
          if (ex == null) return;
          getLogger().log(Level.WARNING, "Sample ban failed for " + args[0], ex);
          bm.scheduler().runSync(() -> sender.sendMessage("Ban failed: " + ex.getMessage()));
        });

    return true;
  }

  private void printCounts(BanManagerService bm, CommandSender sender, boolean fromCommand) {
    DataSource ds = bm.database().localDataSource();
    Optional<String> bansTable = bm.database().localTable("playerBans");
    Optional<String> mutesTable = bm.database().localTable("playerMutes");
    if (bansTable.isEmpty() || mutesTable.isEmpty()) {
      sender.sendMessage("BanManager has not finished provisioning its tables");
      return;
    }

    bm.scheduler().runAsync(() -> {
      try (Connection conn = ds.getConnection()) {
        long bans = countOf(conn, bansTable.get());
        long mutes = countOf(conn, mutesTable.get());
        long denied = countOf(conn, "bm_sample_denied_logins");
        String summary = "[Sample] active player bans=" + bans + ", mutes=" + mutes + ", denied logins=" + denied;
        if (fromCommand) {
          bm.scheduler().runSync(() -> sender.sendMessage(summary));
        } else {
          getLogger().info(summary);
        }
      } catch (Exception ex) {
        getLogger().log(Level.WARNING, "Failed to count rows", ex);
      }
    });
  }

  private static long countOf(Connection conn, String table) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM " + table);
         ResultSet rs = ps.executeQuery()) {
      return rs.next() ? rs.getLong(1) : 0L;
    }
  }

  private static byte[] uuidToBytes(UUID uuid) {
    byte[] bytes = new byte[16];
    long msb = uuid.getMostSignificantBits();
    long lsb = uuid.getLeastSignificantBits();
    for (int i = 0; i < 8; i++) bytes[i] = (byte) (msb >>> (56 - i * 8));
    for (int i = 0; i < 8; i++) bytes[i + 8] = (byte) (lsb >>> (56 - i * 8));
    return bytes;
  }
}
