package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.api.database.DatabaseKind;
import me.confuser.banmanager.api.database.MigrationService;
import me.confuser.banmanager.api.exception.BanManagerException;
import me.confuser.banmanager.api.exception.StorageException;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.storage.migration.MigrationRunner;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Default {@link MigrationService} implementation that delegates to the
 * internal {@link MigrationRunner}.
 *
 * <p>Companion plugins (WebEnhancer, etc.) ship their migrations on their
 * own classpath but reuse BanManager's runner — including the advisory
 * locking that prevents two servers from migrating simultaneously and the
 * placeholder substitution that resolves table-name variables.</p>
 *
 * <p>The caller selects the target database by {@link DatabaseKind} rather
 * than by passing a {@link javax.sql.DataSource}, eliminating an entire
 * class of reference-equality bugs.</p>
 *
 * <h3>Prefix collision detection</h3>
 * <p>The {@link MigrationConfig#prefix()} value scopes rows in the shared
 * {@code bm_schema_version} table. Two plugins picking the same prefix
 * (for example, both shipping {@code "myplugin"}) would silently corrupt
 * each other's version state — every plugin reload would see "current
 * version = whatever the other plugin wrote last" and either skip
 * migrations entirely or replay them from scratch. The
 * {@link MigrationPrefixRegistry} delegate makes that loud rather than
 * silent: a prefix already claimed by a different (still-live)
 * classloader rejects the second registration with a
 * {@link BanManagerException}.</p>
 */
public final class MigrationServiceImpl implements MigrationService {

  private final BanManagerPlugin plugin;
  private final MigrationPrefixRegistry prefixRegistry = new MigrationPrefixRegistry();

  public MigrationServiceImpl(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run(MigrationConfig config) {
    Objects.requireNonNull(config, "config");

    prefixRegistry.claim(config.prefix(), config.classLoader());

    ConnectionSource cs;
    DatabaseConfig dbConfig;
    switch (config.database()) {
      case LOCAL -> {
        cs = plugin.getLocalConn();
        dbConfig = plugin.getConfig().getLocalDb();
      }
      case GLOBAL -> {
        if (plugin.getGlobalDataSource() == null) {
          throw new BanManagerException(
              "Cannot run migrations against the global database — no global database is configured.");
        }
        cs = plugin.getGlobalConn();
        dbConfig = plugin.getConfig().getGlobalDb();
      }
      default -> throw new IllegalStateException("Unhandled DatabaseKind: " + config.database());
    }

    String detectionTableName = config.detectionTable();
    String detectionTableKey = null;
    if (detectionTableName != null && dbConfig.getTables().containsKey(detectionTableName)) {
      // The caller passed a logical key — resolve through dbConfig so the
      // configured (possibly remapped) table name is used.
      detectionTableKey = detectionTableName;
      detectionTableName = null;
    }

    MigrationRunner runner = new MigrationRunner(
        plugin,
        cs,
        dbConfig,
        config.prefix(),
        detectionTableKey,
        detectionTableName,
        config.classLoader(),
        config.resourcePath());

    try {
      runner.migrate();
    } catch (SQLException e) {
      throw new StorageException("Migration failed for prefix '" + config.prefix() + "'", e);
    }
  }
}
