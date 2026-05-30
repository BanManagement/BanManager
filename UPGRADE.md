# Upgrade Notes

End-user- and plugin-author-visible changes that may require attention when
upgrading. Versioned changes are listed newest-first.

---

## v8.0.0 — Stable public API + MiniMessage

v8 is the largest release in BanManager's history. The two most consequential
changes are:

1. **A dedicated public API artifact** (`me.confuser.banmanager:BanManagerAPI`)
   replaces the old `BmAPI` static facade and the per-platform event classes.
2. **MiniMessage / Adventure** replaces the legacy section-symbol colour
   pipeline for kick / chat / broadcast messages.

Server operators upgrading from v7 should read the [Server upgrade](#server-upgrade-v7--v8)
section. Plugin authors integrating against BanManager should read the
[Plugin author upgrade](#plugin-author-upgrade-v7--v8) section.

### Server upgrade (v7 → v8)

#### Required runtime: Java 17 or newer

Same constraint as the v7 → v7.10 modernisation pass; reproduced here for
single-document upgraders.

- BanManager v8 requires **Java 17+** at runtime (Java 21 to build from
  source).
- This matches Spigot/Paper 1.20+, Velocity 3.3+, Sponge API 8+ and Fabric
  1.20.1+ — modern servers need no action.
- Java 8/11 deployments must update their JRE before installing v8.

> **Symptom:** if you start an old JRE against the v8 jar you will see
> `java.lang.UnsupportedClassVersionError: ... has been compiled by a more
> recent version of the Java Runtime (class file version 61.0), this
> version of the Java Runtime only recognises class file versions up to
> 55.0` (or similar) in the server log, followed by the plugin failing to
> load. Class-file `61.0` is Java 17. Upgrade the JRE — there is no
> compatibility shim.

#### Maintenance window guidance

v8 ships several breaking changes that land at once. Plan a single
maintenance window rather than a partial rollout:

1. **Verify the JRE first.** Run `java -version` on the server host before
   touching plugins. If it reports anything below `17`, upgrade the JRE
   and restart the server *before* swapping the BanManager jar — otherwise
   you will hit the `UnsupportedClassVersionError` above and the server
   will start without ban enforcement.
2. **Upgrade BanManager and every BanManager-aware companion plugin in the
   same restart.** A v7 companion plugin (BanManager-WebEnhancer ≤ legacy,
   any plugin compiled against `BanManagerCommon` / `BmAPI` /
   per-platform event classes) will not load against v8 — those classes
   no longer exist. Either upgrade companion jars to releases that target
   `BanManagerAPI:8.x`, recompile your own integrations against the new
   artifact (see [Plugin author upgrade](#plugin-author-upgrade-v7--v8)),
   or remove the incompatible jar before restart.
3. **Deploy in this order on multi-instance setups (proxy + backends):**
   stop all backends, stop the proxy, drop the new jars on every host,
   start the proxy first (so `bungee`/`velocity` registers its API
   instance), then start the backends. Reversing the order produces a
   transient window where backend handlers can call into a v7 proxy API
   and fail with `ClassNotFoundException`.
4. **Custom `messages.yml` overrides need to be re-encoded.** If you
   maintain a customised `messages/*.yml` and skip the conversion to
   MiniMessage, kick messages will display the literal MiniMessage tags
   to players. Run a dry-run with the upgraded jar in a staging server
   first.
5. **Keep the v7 jar handy.** v8 has no in-place downgrade path. See
   [Rollback / downgrade](#rollback--downgrade-v8--v7) below for the
   step-by-step procedure if you need to revert mid-window.

#### Rollback / downgrade (v8 → v7)

v8 ships forward-only schema migrations (the `bm_schema_version` table is
new in v8 and several existing tables get new columns / indexes). There
is **no downgrade migration** — restoring v7 means restoring the database
itself.

**Before** the upgrade, do this on every host that runs BanManager:

1. **Snapshot the BanManager database.** For MySQL/MariaDB:
   ```sh
   mysqldump --single-transaction --quick --routines --triggers \
       --databases banmanager > banmanager-pre-v8.sql
   ```
   Adjust the database name to match your `config.yml`. For H2,
   stop the server first and copy the entire
   `plugins/BanManager/banmanager.mv.db` (and `.trace.db`, `.lock.db` if
   present).
2. **Snapshot the `plugins/BanManager/` directory** — `config.yml`,
   `messages/`, custom translations, exemptions, etc. A `tar -czf
   banmanager-config-pre-v8.tgz plugins/BanManager` is enough.
3. **Keep the previous BanManager v7 jar** alongside the v8 jar so you
   can swap it back without re-downloading.

**To roll back** after a failed v8 startup:

1. Stop the server (and every backend if running on a proxy fleet).
2. Replace the v8 jar with the saved v7 jar in `plugins/`.
3. Restore the SQL snapshot — this **drops and recreates** every table
   the dump contains, undoing v8's schema additions in one step:
   ```sh
   mysql banmanager < banmanager-pre-v8.sql
   ```
   For H2: stop the server, replace the database files with the saved
   copies, then start.
4. Restore `plugins/BanManager/` from the directory snapshot if you
   converted any `messages/*.yml` files to MiniMessage in-place.
5. Restart the proxy first (if applicable), then the backends.

> **Why a SQL restore is required.** v8 may add columns mid-table
> (`ALTER TABLE ... ADD COLUMN`) and create new tables (`bm_schema_version`,
> `bm_player_pins`, etc.). v7 will start against a v8-shaped database, but
> writes to columns it doesn't know about will fail at insert time
> (`Field 'x' doesn't have a default value`) and the new tables will be
> ignored — leaving the database in an unsupported half-and-half state
> that's hard to recover from later. Restoring from the snapshot avoids
> the trap entirely.

A new minor release of v8 (8.x → 8.y) does **not** require a snapshot
unless its release notes call for one — semver applies to internal schema
migrations as well as the public API.

#### MiniMessage replaces section-symbol colour codes

The `messages/` translations and any custom message overrides now use
[MiniMessage](https://docs.advntr.dev/minimessage/index.html) syntax
(`<red>`, `<click>`, `<hover>`) instead of `&c` /`§c` codes.

- Built-in translations are migrated automatically; nothing to do for
  out-of-the-box installs.
- Operators with **customised** `messages/*.yml` files should convert legacy
  codes manually. The official quick-reference is at
  <https://webui.advntr.dev/>.
- Click/hover targets now go through MiniMessage tags
  (e.g. `<click:run_command:'/bmappeal'>Appeal</click>`), so old
  `[Appeal](command://...)` markdown will not render.

#### `messages.yml` symlink replaced by `messages/` directory symlink

- The `messages.yml` symlink that earlier 7.x installs created has been
  replaced by a `messages/` directory containing one file per locale.
- On first start, BanManager will detect the legacy single-file layout and
  migrate it. Verify by checking that `plugins/BanManager/messages/` exists
  and that the legacy `messages.yml` symlink has been removed.

#### Sponge API 7 (Minecraft 1.12.2) is no longer supported

- The `BanManagerSponge` artifact now targets Sponge API 8+. The legacy
  `BanManagerSponge7` module has been dropped.
- Operators on 1.12.2 must either pin to the v7 release branch or upgrade
  their Sponge install.

#### Java 17 modernisation (carried over from v7.10)

The following changes ship in v8 for installs that skipped v7.10. They are
fully transparent for most operators.

- **HikariCP 6.x, ORMLite 6.x, MariaDB JDBC 3.x, MySQL Connector/J 8.x**.
  BanManager generates the JDBC URL per-driver, so the legacy
  `disableMariaDbDriver` workaround has been removed and `useSSL` /
  `verifyServerCertificate` are translated into MariaDB's modern
  `sslMode` (`disable` / `trust` / `verify-full`).
- If `storageType: mysql` is pointed at a MariaDB server, switch to
  `storageType: mariadb` to avoid driver mismatch warnings.
- **SnakeYAML 2.x** with compatibility shims so duplicate keys still resolve
  (last value wins) and the document-size limit is raised to 32 MB.
- **bStats 3.2** and **PlaceholderAPI 2.12** (Bukkit-only soft-dependency).
- **SLF4J 2.x** with a custom service-provider so HikariCP / ORMLite log
  through BanManager's own logger; `disableDatabaseLogging()` is now a no-op
  on Bukkit.

---

### Plugin author upgrade (v7 → v8)

If your plugin imports
`me.confuser.banmanager.common.api.BmAPI`, any
`me.confuser.banmanager.<platform>.api.events.*` class, or any package under
`me.confuser.banmanager.common.*` (`PlayerData`, `Message`, the storage
classes, etc.), v8 is a **breaking change**. Switch to the
`BanManagerAPI` artifact described below — it is the only stable surface and
the only one with a semver guarantee.

#### Switch to the new artifact

The API now lives in its own module so consumer plugins do not pull in
shaded ORMLite / Hikari / Adventure / Caffeine.

`build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("me.confuser.banmanager:BanManagerAPI:8.0.0")
}
```

`pom.xml`:

```xml
<dependency>
    <groupId>me.confuser.banmanager</groupId>
    <artifactId>BanManagerAPI</artifactId>
    <version>8.0.0</version>
    <scope>provided</scope>
</dependency>
```

The artifact has exactly **one** transitive dependency:
`com.github.seancfoley:ipaddress` (unshaded, so the `IPAddress` type the
API exposes is the canonical `inet.ipaddr.IPAddress`).

If you only need to *publish* IP-related requests and don't want a direct
compile-time dependency on `inet.ipaddr`, use the `String`-accepting
overloads on `IpBanRequest`, `IpMuteRequest`, and `IpRangeBanRequest`. The
overloads parse via the bundled library internally and throw
`IllegalArgumentException` on invalid input. Pre-event handlers that read
`request.ip()` still need the dependency to introspect the parsed value.

#### Resolve the service

`BanManager.get()` works on every platform — Bukkit, Bungee, Velocity,
Sponge and Fabric:

```java
import me.confuser.banmanager.api.BanManager;
import me.confuser.banmanager.api.BanManagerService;

BanManagerService bm = BanManager.get();
```

On Bukkit, the service is also published to the Bukkit `ServicesManager`
for plugins that prefer the platform-native lookup:

```java
import org.bukkit.Bukkit;
import me.confuser.banmanager.api.BanManagerService;

BanManagerService bm = Bukkit.getServicesManager().load(BanManagerService.class);
```

`BanManager.get()` throws `IllegalStateException` while BanManager is still
enabling. Use `BanManager.isAvailable()` if your plugin's
`onEnable` may run before BanManager's, or hook `PluginEnableEvent` /
`ProxyInitializedEvent` and resolve there.

#### Async by default + `*Sync` siblings

Every write path on the new services returns a
`CompletableFuture`. Cancelled pre-events resolve to a sentinel
(`Optional.empty()` for create, `false` for delete) on both async and sync
variants — they no longer throw.

```java
import me.confuser.banmanager.api.BanManager;
import me.confuser.banmanager.api.dto.PlayerBan;
import me.confuser.banmanager.api.request.BanRequest;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

UUID playerUuid = ...;
UUID actorUuid = ...;

CompletableFuture<Optional<PlayerBan>> future = BanManager.get().bans().ban(
    new BanRequest(playerUuid, actorUuid, "griefing")
        .expires(System.currentTimeMillis() / 1000L + 3600)
        .silent(false)
);

future.thenAccept(result -> result.ifPresent(ban ->
    getLogger().info("Banned " + ban.player().name() + " until " + ban.expires())
)).exceptionally(ex -> {
    getLogger().log(java.util.logging.Level.WARNING, "Ban write failed", ex);
    return null;
});
```

`CompletableFuture` continuations swallow exceptions silently unless you
attach `.exceptionally(...)` (or `.whenComplete(...)`). Storage failures
(`StorageException` wrapping a `SQLException`) surface as the cause; the
default executor that runs continuations is BanManager's DB-I/O pool, so
do not call platform APIs that require the server tick thread inside an
`exceptionally` handler without scheduling.

If you are already on a worker thread, the `*Sync` variants block:

```java
Optional<PlayerBan> ban = BanManager.get().bans().banSync(
    new BanRequest(playerUuid, actorUuid, "griefing"));
```

Persistence failures surface as `me.confuser.banmanager.api.exception.StorageException`
(an unchecked subclass of `BanManagerException`), wrapping the underlying
`SQLException`. Plugins may inspect `getCause()` to recover the driver
exception. **API methods no longer throw checked exceptions.**

#### `BmAPI` mapping

Every `BmAPI` static method has a 1:1 (sometimes nicer) replacement on the
new service tree. The full mapping:

| v7 `BmAPI` static method                               | v8 replacement                                                                                          |
| ------------------------------------------------------ | ------------------------------------------------------------------------------------------------------- |
| `BmAPI.getPlayer(UUID)`                                | `bm.players().findByUuid(uuid)` / `findByUuidSync(uuid)` returning `Optional<Player>`                    |
| `BmAPI.getPlayer(String)`                              | `bm.players().findByName(name)` / `findByNameSync(name)`                                                 |
| `BmAPI.getPlayers(IPAddress)`                          | `bm.players().findByIp(ip)` / `findByIpSync(ip)` returning `List<Player>`                                |
| `BmAPI.getConsole()`                                   | `bm.players().console()`                                                                                 |
| `BmAPI.toIp(String)`                                   | Use `inet.ipaddr.IPAddressString("…").getAddress()` directly — no longer wrapped                        |
| `BmAPI.ban(player, actor, reason, silent)`             | `bm.bans().ban(new BanRequest(player, actor, reason).silent(silent))`                                    |
| `BmAPI.ban(player, actor, reason, silent, expires)`    | …`.expires(epochSeconds)` on the same request                                                            |
| `BmAPI.unban(ban, actor[, silent])`                    | `bm.bans().unban(uuid, actor, reason, silent)`                                                           |
| `BmAPI.isBanned(uuid|name)`                            | `bm.bans().isBanned(uuid|name)`                                                                          |
| `BmAPI.getCurrentBan(uuid|name)`                       | `bm.bans().findActive(uuid|name)`                                                                        |
| `BmAPI.getBanRecords(player)`                          | `bm.bans().records(uuid, page, size)` returning `Page<PlayerBanRecord>`                                  |
| `BmAPI.mute(...)` (5 overloads)                        | `bm.mutes().mute(new MuteRequest(...).silent(...).soft(...).expires(...))`                               |
| `BmAPI.unmute(mute, actor[, silent])`                  | `bm.mutes().unmute(uuid, actor, reason, silent)`                                                         |
| `BmAPI.isMuted(uuid|name|IPAddress)`                   | `bm.mutes().isMuted(...)` for players, `bm.ipMutes().isMuted(ip)` for IPs                                |
| `BmAPI.getCurrentMute(uuid|name)`                      | `bm.mutes().findActive(uuid|name)`                                                                       |
| `BmAPI.getMuteRecords(player)`                         | `bm.mutes().records(uuid, page, size)` returning `Page<PlayerMuteRecord>`                                |
| `BmAPI.ban(IpBanData)` / IP overloads                  | `bm.ipBans().ban(new IpBanRequest(ip, actor, reason).silent(...).expires(...))`                          |
| `BmAPI.unban(IpBanData, actor[, silent])`              | `bm.ipBans().unban(ip, actor, reason, silent)`                                                           |
| `BmAPI.isBanned(IPAddress)`                            | `bm.ipBans().isBanned(ip)`                                                                               |
| `BmAPI.getCurrentBan(IPAddress)`                       | `bm.ipBans().findActive(ip)`                                                                             |
| `BmAPI.getBanRecords(IPAddress)`                       | `bm.ipBans().records(ip, page, size)` returning `Page<IpBanRecord>`                                      |
| `BmAPI.warn(player, actor, reason, read[, silent])`    | `bm.warnings().warn(new WarnRequest(...).read(read).silent(silent))`                                     |
| `BmAPI.warn(PlayerWarnData[, silent])`                 | Same as above (no longer takes the storage entity)                                                       |
| `BmAPI.getWarnings(player)`                            | `bm.warnings().warnings(uuid, page, size)` returning `Page<PlayerWarn>`                                  |
| `BmAPI.getPlayerNames(uuid)`                           | `bm.history().names(uuid)` / `namesSync(uuid)` returning `List<PlayerNameSummary>`                       |
| `BmAPI.getPlayerHistory(uuid, since, page)`            | `bm.history().sessions(uuid, since, page, size)` returning `Page<PlayerSession>`                         |
| `BmAPI.getPlayerNameAt(uuid, timestamp)`               | `bm.history().nameAt(uuid, timestamp)` / `nameAtSync(uuid, timestamp)`                                   |
| `BmAPI.getMessage(key)`                                | No replacement — message rendering is internal in v8. Build messages via the API record DTOs.            |
| `BmAPI.getLocalConnection()`                           | `bm.database().localDataSource()` returning `javax.sql.DataSource` — **do not close it**                 |
| `BmAPI.toTimestamp(time, future)`                      | No replacement — use `java.time.Duration.parse` or your own helper                                       |

#### Pagination replaces `CloseableIterator<T>`

`CloseableIterator` is gone from the public API. Every paginated query
returns an immutable `Page<T>`:

```java
import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.PlayerBanRecord;

Page<PlayerBanRecord> page = bm.bans().recordsSync(uuid, 0, 50);
page.items().forEach(record -> /* ... */);

if (page.hasMore()) {
    Page<PlayerBanRecord> next = bm.bans().recordsSync(uuid, page.page() + 1, page.size());
}
```

`Page.total()` is `-1` when the storage layer cannot compute it cheaply;
prefer `hasMore()` for pager UIs.

#### Events: subscribe via `EventBus`, not platform listeners

The 31 per-platform event classes (`me.confuser.banmanager.bukkit.api.events.*`,
plus equivalents for Bungee, Velocity, Sponge, Fabric) have all been deleted.
The single replacement is the cross-platform `EventBus`:

| v7 (per-platform)                                        | v8 (cross-platform on `EventBus`)                                |
| -------------------------------------------------------- | ---------------------------------------------------------------- |
| `PlayerBanEvent` (cancellable)                           | `me.confuser.banmanager.api.event.player.PlayerBanEvent`         |
| `PlayerBannedEvent` (post)                               | `me.confuser.banmanager.api.event.player.PlayerBannedEvent`      |
| `PlayerUnbanEvent` / `PlayerUnbannedEvent`               | …`.PlayerUnbanEvent` / …`.PlayerUnbannedEvent`                   |
| `PlayerMuteEvent` / `PlayerMutedEvent`                   | …`.PlayerMuteEvent` / …`.PlayerMutedEvent`                       |
| `PlayerUnmuteEvent` / `PlayerUnmutedEvent`               | …`.PlayerUnmuteEvent` / …`.PlayerUnmutedEvent`                   |
| `PlayerWarnEvent` (now cancellable on every platform)    | …`.PlayerWarnEvent`                                              |
| `PlayerWarnedEvent`                                      | …`.PlayerWarnedEvent`                                            |
| `PlayerNoteCreatedEvent`                                 | …`.PlayerNoteEvent` (cancellable pre-event) + `PlayerNoteCreatedEvent` (post) |
| `PlayerReportEvent` / `PlayerReportedEvent`              | …`.PlayerReportEvent` / `PlayerReportedEvent`                    |
| `PlayerReportDeletedEvent`                               | …`.PlayerReportDeletedEvent`                                     |
| `PlayerKickedEvent`                                      | …`.PlayerKickedEvent`                                            |
| `PlayerDeniedEvent` (login denial — banned/IP/range/name) | …`.PlayerDeniedEvent`                                           |
| `IpBanEvent` / `IpBannedEvent`                           | `me.confuser.banmanager.api.event.ip.IpBanEvent` / `IpBannedEvent` |
| `IpUnbanEvent` / `IpUnbannedEvent`                       | …`.IpUnbanEvent` / `IpUnbannedEvent`                             |
| `IpMuteEvent` / `IpMutedEvent`                           | …`.IpMuteEvent` / `IpMutedEvent`                                 |
| `IpUnmuteEvent` / `IpUnmutedEvent`                       | …`.IpUnmuteEvent` / `IpUnmutedEvent`                             |
| `IpRangeBanEvent` / `IpRangeBannedEvent`                 | …`.IpRangeBanEvent` / `IpRangeBannedEvent`                       |
| `IpRangeUnbanEvent` / `IpRangeUnbannedEvent`             | …`.IpRangeUnbanEvent` / `IpRangeUnbannedEvent`                   |
| `NameBanEvent` / `NameBannedEvent`                       | `me.confuser.banmanager.api.event.name.NameBanEvent` / `NameBannedEvent` |
| `NameUnbanEvent` / `NameUnbannedEvent`                   | …`.NameUnbanEvent` / `NameUnbannedEvent`                         |
| `PluginReloadedEvent`                                    | `me.confuser.banmanager.api.event.player.PluginReloadedEvent`    |
| `SilentEvent` / `SilentCancellableEvent` markers         | Removed — every post-event has a `silent()` accessor             |
| `CustomEvent` / `CustomCancellableEvent` base classes    | Removed — implement `BanManagerEvent` / extend `AbstractCancellableEvent` |

##### Subscription side-by-side

**v7 (Bukkit):**

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import me.confuser.banmanager.bukkit.api.events.PlayerBannedEvent;

public class BanListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBanned(PlayerBannedEvent event) {
        if (!event.isSilent()) {
            getServer().broadcastMessage(event.getBan().getPlayer().getName() + " was banned");
        }
    }
}

// in onEnable():
getServer().getPluginManager().registerEvents(new BanListener(), this);
```

**v8 (works on every platform):**

```java
import me.confuser.banmanager.api.BanManager;
import me.confuser.banmanager.api.event.EventPriority;
import me.confuser.banmanager.api.event.Subscription;
import me.confuser.banmanager.api.event.player.PlayerBannedEvent;

private Subscription bannedSub;

public void onEnable() {
    bannedSub = BanManager.get().events().subscribe(
        PlayerBannedEvent.class,
        EventPriority.MONITOR,
        event -> {
            if (!event.silent()) {
                getServer().broadcastMessage(event.ban().player().name() + " was banned");
            }
        });
}

public void onDisable() {
    if (bannedSub != null) bannedSub.unsubscribe();
}
```

Notes:

- Event dispatch is synchronous on the publishing thread, preserving v7
  `callEvent(...)` semantics. Because BanManager's storage layer publishes
  events from its own dedicated DB-I/O executor, your handlers run **off**
  the main thread by default. Two consequences:
  1. **Subscribers must return promptly** — target budget under
     ~10&nbsp;ms, never any blocking I/O. A slow handler holds up *every*
     subsequent ban / mute / warn write because it pins the DB-I/O
     executor's worker. Offload heavy work to `bm.scheduler().runAsync(...)`
     and return.
  2. **Do not call platform APIs that require the server tick thread**
     (Bukkit `World`, etc.) without first hopping back via
     `bm.scheduler().runSync(...)` — and only on platforms where
     `scheduler.isMainThreadAware()` returns `true` (Bukkit/Sponge/Fabric).
- Always retain the `Subscription` and call `unsubscribe()` on plugin
  disable / reload. BanManager unsubscribes its own listeners on
  `/bmreload`; the post-reload `PluginReloadedEvent` is your cue to
  re-register.
- Pre-events expose a mutable `request()` payload (e.g.
  `PlayerBanEvent#request()` returns the `BanRequest` that will be
  persisted). Modify it freely; cancel via `event.cancel()`. The
  cancellation surfaces to async callers as
  `CompletableFuture<Optional<...>>` resolving empty.

##### Mutating kick messages from a handler (PIN injection, etc.)

Five post-events expose a mutable `placeholders()` map that BanManager
applies to the kick message template before disconnecting an online player:

- `PlayerDeniedEvent`
- `PlayerBannedEvent`
- `IpBannedEvent`
- `IpRangeBannedEvent`
- `NameBannedEvent`

This is the supported way to inject template variables (e.g. `<pin>`,
`<appeal_url>`) without touching BanManager internals — the same hook
WebEnhancer uses.

```java
BanManager.get().events().subscribe(PlayerDeniedEvent.class, event ->
    event.placeholders().put("appeal_url", "https://bans.example.com/appeal"));
```

Then in `messages/en.yml`:

```yaml
ban:
  player:
    disallowed: "<red>Banned: <reason></red>\n<gray>Appeal at <click:open_url:'<appeal_url>'><appeal_url></click>"
```

#### Exception migration

| v7 throws                                                | v8 surface                                                                          |
| -------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| `java.sql.SQLException` (checked)                        | `me.confuser.banmanager.api.exception.StorageException` (unchecked, wraps the cause) |
| `RuntimeException` from cancelled events                 | Async **and** sync: returns `Optional.empty()` for create-style operations or `false` for delete-style operations. **Cancellation is not exceptional** — `OperationCancelledException` is reserved for future use and is not thrown by the v8 API. |
| `IllegalArgumentException` from invalid input            | Same — preserved on the API where it makes sense                                    |
| Misc internal exceptions                                 | `BanManagerException` (root unchecked type — catch this for a wide net)             |

Existing `try { ... } catch (SQLException e) { ... }` blocks must drop the
checked exception or switch to `catch (StorageException e)`.

#### Database access

`BmAPI.getLocalConnection()` returned an internal ORMLite `ConnectionSource`.
v8 returns a standard `javax.sql.DataSource` instead so consumers do not
have to depend on shaded ORMLite:

```java
import javax.sql.DataSource;
import me.confuser.banmanager.api.BanManager;

DataSource ds = BanManager.get().database().localDataSource();
try (var conn = ds.getConnection();
     var ps = conn.prepareStatement("SELECT count(*) FROM " +
         BanManager.get().database().localTable("playerBans").orElseThrow())) {
    // ...
}
```

`localTable("playerBans")` resolves the operator's configured table name
(operators may override individual tables in `config.yml`, e.g. for shared
WordPress prefixes). **Never close the returned `DataSource`** — it is owned
by BanManager and shared across the JVM.

The pool is **not sandboxed** — it exposes the same DB privileges as
BanManager itself (typically full DDL/DML on the configured database).
Reads against any table are safe; writes against tables your plugin owns
are safe; writes against `bm_*` tables will silently desync the in-memory
cache, the event bus, and global-sync replication — use the service
sub-services for those instead.

If your plugin needs the global (cross-server) database, use
`bm.database().globalDataSource()` which returns
`Optional<DataSource>` (empty when global sync isn't configured).

#### Database migrations for companion plugins

Plugins that ship their own SQL migrations can run them against a
BanManager-owned database by calling `MigrationService`:

```java
import me.confuser.banmanager.api.BanManager;
import me.confuser.banmanager.api.database.DatabaseKind;
import me.confuser.banmanager.api.database.MigrationService.MigrationConfig;

BanManager.get().migrations().run(new MigrationConfig(
    DatabaseKind.LOCAL,
    "myplugin",
    "db/myplugin",
    getClass().getClassLoader()
));
```

Resources expected at `db/myplugin/` on the classpath:

- `migrations.list` — newline-separated list of migration filenames
- `V1__initial_schema.sql`, `V2__add_index.sql`, …

The runner is idempotent at the migration-file level and tracks applied
versions in a shared `bm_schema_version` table, namespaced by the `prefix`
argument. Statements within a file execute one at a time and are **not**
wrapped in a transaction (MySQL/MariaDB DDL implicitly commits anyway), so
a failure mid-file leaves earlier statements applied. The version row is
only inserted after every statement in the file succeeds, so re-running
restarts the failed migration from statement #1 — write idempotent
statements (`CREATE TABLE IF NOT EXISTS`, `ADD COLUMN IF NOT EXISTS`, etc.)
or split non-idempotent steps into their own `V*__*.sql` files so a retry
resumes from a clean state.

Choose a `prefix` value that's unique to your plugin — BanManager rejects
two registrations of the same prefix from different plugin classloaders
to avoid silent `bm_schema_version` collisions.

#### ORMLite shading guidance

If you previously depended on `BanManagerCommon` and reflectively touched
storage classes (`PlayerBanStorage`, `PlayerData`, `Message`, etc.) you have
two options:

1. **Recommended** — switch to the new `BanManagerAPI` artifact. Every read
   path you needed has a stable replacement (see the mapping table above)
   and you no longer have to deal with shaded packages or classloader
   visibility issues.
2. **Companion-plugin escape hatch** — if you genuinely need to share
   BanManager's ORMLite instance (e.g. you store entities related to
   `PlayerData` and want them in the same database / connection pool):
   - Continue depending on `BanManagerCommon` as `compileOnly` /
     `provided` and resolve the `BanManagerPlugin` instance via the
     platform's plugin manager (e.g.
     `Bukkit.getPluginManager().getPlugin("BanManager")` →
     `((BMBukkitPlugin) plugin).getPlugin()`).
   - Pull `ConnectionSource` from `banManagerPlugin.getLocalConn()`.
   - Be aware: `BanManagerCommon` shades ORMLite under
     `me.confuser.banmanager.common.ormlite.*`. Do **not** also shade the
     vanilla `com.j256.ormlite.*` package — your DAOs must use the shaded
     types when sharing the connection.
   - This is the path BanManager-WebEnhancer uses; see its source for a
     worked example.

#### Removed: `BanManagerPlugin.getInstance()`

The static `getInstance()` accessor on `BanManagerPlugin` is gone. Resolve
the instance via the platform plugin manager as shown above. The new
`BanManager.get()` is the supported public path; `BanManagerPlugin` itself
remains an internal class with no semver guarantee.

#### Schedulers

Use `bm.scheduler()` (see
`me.confuser.banmanager.api.scheduler.BanManagerScheduler`) for
fire-and-forget async work. **Do not** use it for blocking JDBC — every
write path on the API already returns `CompletableFuture` and runs on a
dedicated DB-I/O executor; submitting blocking work via `runAsync` on
Sponge or Fabric runs on the platform's `ForkJoinPool` and can starve other
plugins.

`scheduler.runSync` runs on the server tick thread on Bukkit, Sponge and
Fabric, and is documented to alias `runAsync` on Bungee / Velocity (which
are asynchronous proxies). Use `scheduler.isMainThreadAware()` when your
task requires single-threaded ordering.

---

## v7.10 — Java 17 modernisation

This release modernised the codebase from Java 8 to Java 17 and bumped a
number of bundled dependencies. v8 carries every change in this section
forward; v7 → v8 upgraders can read the v8 [Server upgrade](#server-upgrade-v7--v8)
section, which folds these notes in.

### Required runtime: Java 17 or newer

- BanManager now requires **Java 17+** at runtime (Java 21 to build from
  source).
- This matches the supported runtimes for Spigot/Paper 1.20+, Velocity 3.3+,
  and Sponge API 8+, so no action is normally required for modern servers.
- If you are still running Java 8/11 you must update your JRE before
  installing this version.

### MariaDB JDBC driver upgraded to 3.x

- The bundled `mariadb-java-client` is now `3.5.x`. The 3.x driver no longer
  hijacks `jdbc:mysql://` URLs, and it warns about legacy parameters that the
  2.x line silently accepted.
- BanManager now builds a per-driver JDBC URL automatically based on
  `storageType` in `config.yml`, so you should not see `WARN` lines about
  unknown options like `autoReconnect`, `serverTimezone`, or
  `verifyServerCertificate` after upgrading.
- `useSSL` and `verifyServerCertificate` are translated to MariaDB's
  `sslMode` (`disable`, `trust`, or `verify-full`). No `config.yml` changes
  are required.
- If you previously set `storageType: mysql` but pointed at a MariaDB server,
  consider switching to `storageType: mariadb` so the correct driver is used.

### MySQL Connector/J upgraded to 8.4.x

- The shaded `mysql-connector-j` is now `8.4.0`, replacing the legacy
  `mysql-connector-java` artifact.
- The legacy `&disableMariaDbDriver` URL fragment has been removed because the
  modern MariaDB driver no longer needs to be opted out.

### SnakeYAML upgraded to 2.x

- The bundled SnakeYAML jumped from `1.29` to `2.4`. SnakeYAML 2.x flips a
  handful of defaults that could otherwise break existing user-edited
  configs. BanManager pre-configures the loader to keep the old behaviour:
  - `allowDuplicateKeys` is forced back to `true` so a duplicate key in
    `messages.yml` won't refuse to load (the last value wins, as before).
  - `codePointLimit` is raised to 32 MB so very large translation files keep
    loading.
  - `nestingDepthLimit` is raised to 100 for deeply nested webhook payloads.
- If you intentionally relied on duplicate-key detection, consider linting
  your YAML separately.

### Bundled bStats / PlaceholderAPI upgrades

- bStats was bumped to `3.2.1` across all platforms.
- PlaceholderAPI was bumped to `2.12.2` (Bukkit only, soft-dependency).

### SLF4J upgraded to 2.x on Bukkit

- The Bukkit module now ships an SLF4J 2.x service-provider implementation so
  that ORMLite and HikariCP log through BanManager's own logger rather than
  the generic console.
- `disableDatabaseLogging()` is now a no-op on Bukkit (the new provider
  filters log levels itself).

### Tests: JUnit 5 + Mockito 5

- Internal change only — the test suite migrated from JUnit 4/Mockito 3 to
  JUnit 5/Mockito 5. No effect on the runtime jar.
