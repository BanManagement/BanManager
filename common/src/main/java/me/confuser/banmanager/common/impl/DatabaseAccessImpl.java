package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.api.database.DatabaseAccess;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.hikari.HikariDataSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;

import javax.sql.DataSource;

import java.util.Optional;

/**
 * {@link DatabaseAccess} backed by BanManager's internal Hikari pools. The
 * {@code HikariDataSource} instances are owned by {@link BanManagerPlugin}
 * and live for the duration of the plugin — never close them.
 */
public final class DatabaseAccessImpl implements DatabaseAccess {

  private final BanManagerPlugin plugin;

  public DatabaseAccessImpl(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public DataSource localDataSource() {
    HikariDataSource ds = plugin.getLocalDataSource();
    if (ds == null) {
      throw new IllegalStateException(
          "Local datasource not yet initialised. Call DatabaseAccess after BanManager has finished enabling.");
    }
    return ds;
  }

  @Override
  public Optional<DataSource> globalDataSource() {
    return Optional.ofNullable(plugin.getGlobalDataSource());
  }

  @Override
  public Optional<String> localTable(String logicalName) {
    return tableName(plugin.getConfig().getLocalDb(), logicalName);
  }

  @Override
  public Optional<String> globalTable(String logicalName) {
    DatabaseConfig global = plugin.getConfig().getGlobalDb();
    if (!global.isEnabled()) return Optional.empty();
    return tableName(global, logicalName);
  }

  private static Optional<String> tableName(DatabaseConfig dbConfig, String logicalName) {
    DatabaseTableConfig<?> table = dbConfig.getTable(logicalName);
    if (table == null) return Optional.empty();
    return Optional.ofNullable(table.getTableName());
  }
}
