# Upgrade Notes

End-user-visible changes that may require attention when upgrading. Versioned
changes are listed newest-first.

## Java 17 modernisation

This release modernises the codebase from Java 8 to Java 17 and bumps a number
of bundled dependencies. Most installs upgrade transparently, but the items
below are worth checking.

### Required runtime: Java 17 or newer

- BanManager now requires **Java 17+** at runtime (Java 21 to build from
  source).
- This matches the supported runtimes for Spigot/Paper 1.20+, Velocity 3.3+,
  and Sponge API 8+, so no action is normally required for modern servers.
- If you are still running Java 8/11 you must update your JRE before installing
  this version.

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

- Internal change only - the test suite migrated from JUnit 4/Mockito 3 to
  JUnit 5/Mockito 5. No effect on the runtime jar.
