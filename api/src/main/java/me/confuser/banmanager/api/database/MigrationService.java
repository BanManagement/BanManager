package me.confuser.banmanager.api.database;

/**
 * Runs SQL migration scripts bundled inside a plugin's JAR against an
 * arbitrary database. Reuses BanManager's MariaDB-aware migration runner so
 * companion plugins (WebEnhancer, etc.) do not have to ship their own.
 */
public interface MigrationService {

  /**
   * Run the migrations described by {@code config}. Idempotent at the
   * <em>file</em> level — applied migrations are tracked in the shared
   * {@code bm_schema_version} table, scoped by the
   * {@link MigrationConfig#prefix()} value, so re-running this method
   * applies only the deltas that haven't been recorded yet.
   *
   * <h3>Concurrency</h3>
   * <p>The runner takes a database-level advisory lock
   * ({@code GET_LOCK('bm_migration_<prefix>', 30)} on MySQL/MariaDB) for
   * the duration of the run, so a clustered deployment will not
   * double-apply migrations even if two nodes restart simultaneously. The
   * lock is skipped on H2 (single-process anyway).</p>
   *
   * <h3>Per-statement semantics — IMPORTANT</h3>
   * <p>Statements within a single migration file execute
   * <strong>one at a time</strong> and are <strong>not</strong> wrapped in a
   * JDBC transaction. On MySQL/MariaDB this is a hard limitation: DDL
   * (CREATE/ALTER/DROP) implicitly commits. The runner has the following
   * partial-failure behaviour:</p>
   * <ul>
   *   <li>If a statement throws and the migration is <b>not</b> marked
   *       {@code lenient} in {@code migrations.list}, the exception
   *       propagates as {@code StorageException} and the version row is
   *       <strong>not</strong> inserted. Re-running this method will
   *       restart the failed migration from statement #1.</li>
   *   <li>If the migration <b>is</b> marked {@code lenient}, individual
   *       statement failures are logged and the run continues; the version
   *       row is still inserted at the end.</li>
   * </ul>
   *
   * <p>The combination of advisory locking + late version-row insert means
   * a partial failure cannot corrupt the
   * {@code bm_schema_version} table — but the database itself can end up
   * with half a migration applied (e.g. a new column exists but the
   * follow-up index does not). To make re-runs safe, write idempotent
   * statements:</p>
   * <pre>{@code
   * CREATE TABLE IF NOT EXISTS my_plugin_audit (...);
   * ALTER TABLE my_plugin_audit ADD COLUMN IF NOT EXISTS ip VARBINARY(16);
   * CREATE INDEX IF NOT EXISTS idx_audit_actor ON my_plugin_audit (actor);
   * }</pre>
   *
   * <p>Where the SQL dialect lacks {@code IF NOT EXISTS} for a particular
   * DDL form (older MariaDB indexes, for example), prefer splitting the
   * non-idempotent step into its own {@code V*__*.sql} file so a retry
   * resumes from a clean state.</p>
   *
   * <h3>Prefix scoping</h3>
   * <p>Choose a distinct {@link MigrationConfig#prefix()} per plugin —
   * BanManager rejects two registrations of the same prefix from different
   * plugin classloaders to avoid silent {@code bm_schema_version}
   * collisions.</p>
   *
   * @throws me.confuser.banmanager.api.exception.StorageException
   *         when the migration fails (cause holds the SQL exception)
   * @throws me.confuser.banmanager.api.exception.BanManagerException
   *         when the requested database is not configured (e.g.
   *         {@link DatabaseKind#GLOBAL} when no global DB is set up), or
   *         when the {@code prefix} collides with another plugin's
   *         registration
   */
  void run(MigrationConfig config);

  /**
   * Configuration for {@link MigrationService#run(MigrationConfig)}.
   *
   * @param database which BanManager-owned database to migrate. The
   *                 {@link DatabaseKind} is resolved internally to the
   *                 matching pool and ORMLite {@code ConnectionSource} so
   *                 no {@link javax.sql.DataSource} round-tripping is
   *                 required (and reference-equality bugs are impossible).
   * @param prefix unique identifier for this set of migrations, used to
   *               namespace rows in the shared {@code bm_schema_version}
   *               table (e.g. {@code "webenhancer"})
   * @param resourcePath classpath directory containing
   *                     {@code migrations.list} and the {@code V*__*.sql}
   *                     files (e.g. {@code "db/webenhancer"})
   * @param classLoader classloader to load the resources from (typically
   *                    the caller's plugin classloader)
   * @param detectionTable optional logical-table name (matching a key in
   *                       {@link DatabaseAccess#localTable(String)}) that
   *                       must exist before any migrations are applied; when
   *                       it is absent the runner marks the install as fresh
   *                       and stamps the latest version without executing
   *                       any SQL. Pass {@code null} to always apply
   *                       migrations from V1.
   */
  record MigrationConfig(DatabaseKind database, String prefix, String resourcePath,
                         ClassLoader classLoader, String detectionTable) {

    public MigrationConfig {
      if (database == null) throw new IllegalArgumentException("database");
      if (prefix == null) throw new IllegalArgumentException("prefix");
      if (resourcePath == null) throw new IllegalArgumentException("resourcePath");
      if (classLoader == null) throw new IllegalArgumentException("classLoader");
    }

    public MigrationConfig(DatabaseKind database, String prefix, String resourcePath, ClassLoader classLoader) {
      this(database, prefix, resourcePath, classLoader, null);
    }
  }
}
