# BanManager sample plugin

A tiny Bukkit consumer plugin that exercises a representative slice of
`BanManagerAPI` — service resolution, async writes, event subscription
with placeholder injection, plugin-owned migrations, raw `DataSource`
access, and cross-platform scheduling — so the published artifact is
verified against a real consumer plugin on every CI run. It is not a
soak test of every sub-service (mutes, warnings, IP / range / name bans,
notes, reports, history are not exercised); it's the smallest plugin
that proves the build wiring is correct, kept small enough to read
cover-to-cover.

## What it demonstrates

| Feature                              | Where in the code                                                         |
| ------------------------------------ | ------------------------------------------------------------------------- |
| `BanManager.get()` resolution        | `SamplePlugin#onEnable`                                                   |
| Bukkit `ServicesManager` lookup      | `SamplePlugin#onEnable` (cross-checked against `BanManager.get()`)        |
| `MigrationService` for plugin tables | `SamplePlugin#runMigrations` + `resources/db/sample/`                     |
| `EventBus` subscription              | `SamplePlugin#registerEventSubscriptions`                                 |
| Placeholder injection                | `PlayerDeniedEvent` handler — pushes `<appeal_url>` into `placeholders()` |
| `PluginReloadedEvent` re-registration | `SamplePlugin#registerEventSubscriptions`                                |
| Async `BanService` calls             | `SamplePlugin#handleSampleBan`                                            |
| `PlayerService` lookup (async)       | `SamplePlugin#handleSampleBan`                                            |
| `DatabaseAccess` `DataSource`        | `SamplePlugin#printCounts` + `recordDeniedLogin`                          |
| `localTable("…")` lookup             | `SamplePlugin#printCounts`                                                |
| Cross-platform scheduler             | `SamplePlugin#schedulePeriodicCounts` and `runSync` thread-handoff sites  |

## Build

The sample plugin is wired into the BanManager Gradle build:

```bash
./gradlew :BanManagerSamplePlugin:shadowJar
```

The resulting jar lands in `e2e/sample-plugin/build/libs/BanManagerSamplePlugin.jar`.

## Run on a Paper server

1. Drop `BanManagerBukkit.jar` and `BanManagerSamplePlugin.jar` into your
   server's `plugins/` directory.
2. Start the server. BanManager enables first; the sample plugin then
   resolves the service and registers its subscriptions.
3. Useful in-game commands:
   - `/sampleban <player> [reason]` — bans via `BanRequest`, shows the
     async future surface.
   - `/samplecount` — issues a custom SQL query through the shared
     `DataSource`.

A row is appended to `bm_sample_denied_logins` every time a banned UUID,
banned IP, banned IP range, or banned name attempts to log in — verifying
both the `PlayerDeniedEvent` subscription and the `MigrationService`
bootstrap.
