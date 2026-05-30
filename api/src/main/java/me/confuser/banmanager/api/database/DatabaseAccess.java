package me.confuser.banmanager.api.database;

import javax.sql.DataSource;

import java.util.Optional;

/**
 * Direct access to the {@link DataSource}s that back BanManager. Useful for
 * companion plugins that need to issue custom queries against BanManager's
 * tables (e.g. BanManager-WebEnhancer for its forum-specific reporting
 * queries) or that ship their own tables in the same database (typically
 * created via {@link MigrationService}).
 *
 * <h2>Privilege scope</h2>
 * <p>The returned pools are <strong>not sandboxed</strong>. They expose the
 * same database privileges as BanManager itself — typically full
 * {@code SELECT/INSERT/UPDATE/DELETE/DDL} on the configured database — so a
 * misbehaving consumer can mutate {@code bm_*} tables out from under the
 * service layer. Treat them like raw JDBC:</p>
 *
 * <ul>
 *   <li><b>Reads against any table</b> — safe.</li>
 *   <li><b>Writes against tables your plugin owns</b> (typically created via
 *       {@link MigrationService}) — safe.</li>
 *   <li><b>Writes against {@code bm_*} tables</b> — <b>do not</b>. Use the
 *       {@code BanManagerService} sub-services instead so the cache layer,
 *       event bus, and global-sync replication stay coherent.</li>
 * </ul>
 *
 * <p><b>Do not close the returned {@link DataSource} instances</b>; they are
 * owned by BanManager and shared across the JVM. Closing one shuts the pool
 * for every consumer (including BanManager itself).</p>
 *
 * <h2>Table name lookup</h2>
 * <p>BanManager allows operators to rename individual tables in
 * {@code config.yml} (e.g. for WordPress-style {@code wp_} prefixes); use
 * {@link #localTable(String)} / {@link #globalTable(String)} to resolve a
 * logical key like {@code "playerBans"} to the configured SQL table name.
 * The full list of logical keys is documented under {@code config.yml ->
 * databases.local.tables}.</p>
 */
public interface DatabaseAccess {

  /**
   * @return the local (single-server) database that BanManager always uses
   */
  DataSource localDataSource();

  /**
   * @return the optional global database, when the operator has configured
   *         cross-server sync. Empty otherwise.
   */
  Optional<DataSource> globalDataSource();

  /**
   * Resolve a logical table key (e.g. {@code "players"}, {@code "playerBans"})
   * to the configured SQL table name in the local database.
   *
   * @return the SQL table name, or empty when the key is unknown
   */
  Optional<String> localTable(String logicalName);

  /**
   * Same as {@link #localTable(String)} but for the global database.
   */
  Optional<String> globalTable(String logicalName);
}
