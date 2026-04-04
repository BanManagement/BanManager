package me.confuser.banmanager.common.storage.migration;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.ormlite.field.SqlType;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.support.CompiledStatement;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;
import me.confuser.banmanager.common.ormlite.table.TableUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MigrationRunner {

  private static final Pattern VERSION_PATTERN = Pattern.compile("^V(\\d+)__(.+)\\.sql$");
  static final String SCHEMA_TABLE = "bm_schema_version";

  private final BanManagerPlugin plugin;
  private final ConnectionSource connectionSource;
  private final DatabaseConfig dbConfig;
  private final String scope;
  private final String detectionTableKey;
  private final ClassLoader resourceLoader;

  /**
   * The instance-qualified scope used for database operations (version tracking, advisory locks).
   * Combines the resource scope with the detection table name to isolate instances that share
   * the same database but use different table prefixes (e.g. "local:bm_players" vs "local:bm_s2_players").
   */
  private String instanceScope;

  public MigrationRunner(BanManagerPlugin plugin, ConnectionSource connectionSource,
                         DatabaseConfig dbConfig, String scope, String detectionTableKey,
                         ClassLoader resourceLoader) {
    this.plugin = plugin;
    this.connectionSource = connectionSource;
    this.dbConfig = dbConfig;
    this.scope = scope;
    this.detectionTableKey = detectionTableKey;
    this.resourceLoader = resourceLoader;
  }

  public void migrate() throws SQLException {
    List<MigrationFile> migrations = loadManifest();
    if (migrations.isEmpty()) {
      plugin.getLogger().info("[Migration:" + scope + "] No migrations found in manifest");
      return;
    }

    String detectionTableName = dbConfig.getTable(detectionTableKey).getTableName();
    instanceScope = scope + ":" + detectionTableName;

    int latestVersion = migrations.get(migrations.size() - 1).version;
    boolean isH2 = dbConfig.getStorageType().equals("h2");

    DatabaseConnection conn = connectionSource.getReadWriteConnection("");
    try {
      if (!isH2) {
        acquireAdvisoryLock(conn);
      }

      try {
        TableUtils.createTableIfNotExists(connectionSource, SchemaVersion.class);

        if (!tableExists(conn, detectionTableName)) {
          plugin.getLogger().info("[Migration:" + instanceScope + "] Fresh install detected, marking schema at V" + latestVersion);
          insertVersion(conn, latestVersion, "baseline (fresh install)");
          return;
        }

        int currentVersion = getCurrentVersion(conn);

        if (currentVersion == 0) {
          plugin.getLogger().info("[Migration:" + instanceScope + "] Existing install detected, marking V1 as baseline");
          insertVersion(conn, 1, "baseline (existing install)");
          currentVersion = 1;
        }

        int applied = 0;
        for (MigrationFile migration : migrations) {
          if (migration.version <= currentVersion) {
            continue;
          }

          plugin.getLogger().info("[Migration:" + instanceScope + "] Applying V" + migration.version + " " + migration.description);
          String sql = loadSqlFile(migration.filename);
          if (sql.isEmpty()) {
            throw new SQLException("[Migration:" + instanceScope + "] Migration file not found or empty: " + migration.filename);
          }
          sql = substitutePlaceholders(sql);
          executeMigrationStatements(conn, sql);
          insertVersion(conn, migration.version, migration.description);
          applied++;
        }

        if (applied > 0) {
          plugin.getLogger().info("[Migration:" + instanceScope + "] Applied " + applied + " migration(s)");
        }
      } finally {
        if (!isH2) {
          releaseAdvisoryLock(conn);
        }
      }
    } finally {
      connectionSource.releaseConnection(conn);
    }
  }

  private void acquireAdvisoryLock(DatabaseConnection conn) throws SQLException {
    CompiledStatement stmt = conn.compileStatement(
        "SELECT GET_LOCK('bm_migration_" + instanceScope + "', 30)",
        StatementBuilder.StatementType.SELECT, null,
        DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
    DatabaseResults results = stmt.runQuery(null);
    if (!results.next() || results.getInt(0) != 1) {
      throw new SQLException("[Migration:" + instanceScope + "] Could not acquire advisory lock (another server may be migrating)");
    }
  }

  private void releaseAdvisoryLock(DatabaseConnection conn) {
    try {
      conn.executeStatement("SELECT RELEASE_LOCK('bm_migration_" + instanceScope + "')",
          DatabaseConnection.DEFAULT_RESULT_FLAGS);
    } catch (SQLException e) {
      plugin.getLogger().warning("[Migration:" + instanceScope + "] Failed to release advisory lock", e);
    }
  }

  private List<MigrationFile> loadManifest() {
    List<MigrationFile> migrations = new ArrayList<>();
    String manifestPath = "db/" + scope + "/migrations.list";

    try (InputStream is = resourceLoader.getResourceAsStream(manifestPath)) {
      if (is == null) {
        plugin.getLogger().warning("[Migration:" + scope + "] No manifest found at " + manifestPath);
        return migrations;
      }

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty() || line.startsWith("#")) {
            continue;
          }

          Matcher matcher = VERSION_PATTERN.matcher(line);
          if (matcher.matches()) {
            int version = Integer.parseInt(matcher.group(1));
            String description = matcher.group(2).replace('_', ' ');
            migrations.add(new MigrationFile(line, version, description));
          } else {
            plugin.getLogger().warning("[Migration:" + scope + "] Skipping invalid manifest entry: " + line);
          }
        }
      }
    } catch (IOException e) {
      plugin.getLogger().warning("[Migration:" + scope + "] Failed to read manifest: " + e.getMessage());
    }

    migrations.sort(Comparator.comparingInt(m -> m.version));
    return migrations;
  }

  private boolean tableExists(DatabaseConnection conn, String tableName) {
    try {
      conn.executeStatement("SELECT 1 FROM `" + tableName + "` LIMIT 1",
          DatabaseConnection.DEFAULT_RESULT_FLAGS);
      return true;
    } catch (SQLException e) {
      return false;
    }
  }

  private int getCurrentVersion(DatabaseConnection conn) throws SQLException {
    try {
      CompiledStatement stmt = conn.compileStatement(
          "SELECT COALESCE(MAX(version), 0) FROM " + SCHEMA_TABLE + " WHERE scope = ?",
          StatementBuilder.StatementType.SELECT, null,
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
      stmt.setObject(0, instanceScope, SqlType.STRING);
      DatabaseResults results = stmt.runQuery(null);
      if (results.next()) {
        return results.getInt(0);
      }
    } catch (SQLException e) {
      // Table may not exist yet
    }
    return 0;
  }

  private String loadSqlFile(String filename) {
    String path = "db/" + scope + "/" + filename;

    try (InputStream is = resourceLoader.getResourceAsStream(path)) {
      if (is == null) {
        plugin.getLogger().warning("[Migration:" + scope + "] SQL file not found: " + path);
        return "";
      }

      byte[] bytes = readAllBytes(is);
      return new String(bytes, StandardCharsets.UTF_8);
    } catch (IOException e) {
      plugin.getLogger().warning("[Migration:" + scope + "] Failed to read SQL file: " + path);
      return "";
    }
  }

  private String substitutePlaceholders(String sql) {
    for (Map.Entry<String, me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig<?>> entry
        : dbConfig.getTables().entrySet()) {
      sql = sql.replace("${" + entry.getKey() + "}", entry.getValue().getTableName());
    }
    return sql;
  }

  private void executeMigrationStatements(DatabaseConnection conn, String sql) {
    List<String> statements = splitStatements(sql);

    for (String statement : statements) {
      try {
        conn.executeStatement(statement, DatabaseConnection.DEFAULT_RESULT_FLAGS);
      } catch (SQLException e) {
        plugin.getLogger().warning("[Migration:" + instanceScope + "] Statement failed (continuing): " + e.getMessage());
      }
    }
  }

  static List<String> splitStatements(String sql) {
    List<String> statements = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;
    boolean inLineComment = false;
    boolean inBlockComment = false;

    for (int i = 0; i < sql.length(); i++) {
      char c = sql.charAt(i);
      char next = (i + 1 < sql.length()) ? sql.charAt(i + 1) : '\0';

      if (inLineComment) {
        if (c == '\n') {
          inLineComment = false;
          current.append(c);
        }
        continue;
      }

      if (inBlockComment) {
        if (c == '*' && next == '/') {
          inBlockComment = false;
          i++;
        }
        continue;
      }

      if (c == '-' && next == '-' && !inSingleQuote && !inDoubleQuote) {
        inLineComment = true;
        i++;
        continue;
      }

      if (c == '/' && next == '*' && !inSingleQuote && !inDoubleQuote) {
        inBlockComment = true;
        i++;
        continue;
      }

      if (c == '\\' && (inSingleQuote || inDoubleQuote)) {
        current.append(c);
        if (next != '\0') {
          current.append(next);
          i++;
        }
        continue;
      }

      if (c == '\'' && !inDoubleQuote) {
        inSingleQuote = !inSingleQuote;
      } else if (c == '"' && !inSingleQuote) {
        inDoubleQuote = !inDoubleQuote;
      }

      if (c == ';' && !inSingleQuote && !inDoubleQuote) {
        String stmt = current.toString().trim();
        if (!stmt.isEmpty()) {
          statements.add(stmt);
        }
        current.setLength(0);
      } else {
        current.append(c);
      }
    }

    String remaining = current.toString().trim();
    if (!remaining.isEmpty()) {
      statements.add(remaining);
    }

    return statements;
  }

  private void insertVersion(DatabaseConnection conn, int version, String description) throws SQLException {
    long appliedAt = System.currentTimeMillis() / 1000L;
    CompiledStatement stmt = conn.compileStatement(
        "INSERT INTO " + SCHEMA_TABLE + " (version, description, appliedAt, scope) VALUES (?, ?, ?, ?)",
        StatementBuilder.StatementType.UPDATE, null,
        DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
    stmt.setObject(0, version, SqlType.INTEGER);
    stmt.setObject(1, description, SqlType.STRING);
    stmt.setObject(2, appliedAt, SqlType.LONG);
    stmt.setObject(3, instanceScope, SqlType.STRING);
    stmt.runUpdate();
  }

  private static byte[] readAllBytes(InputStream is) throws IOException {
    byte[] buffer = new byte[4096];
    int bytesRead;
    List<byte[]> chunks = new ArrayList<>();
    int totalLen = 0;

    while ((bytesRead = is.read(buffer)) != -1) {
      byte[] chunk = new byte[bytesRead];
      System.arraycopy(buffer, 0, chunk, 0, bytesRead);
      chunks.add(chunk);
      totalLen += bytesRead;
    }

    byte[] result = new byte[totalLen];
    int offset = 0;
    for (byte[] chunk : chunks) {
      System.arraycopy(chunk, 0, result, offset, chunk.length);
      offset += chunk.length;
    }

    return result;
  }

  static class MigrationFile {
    final String filename;
    final int version;
    final String description;

    MigrationFile(String filename, int version, String description) {
      this.filename = filename;
      this.version = version;
      this.description = description;
    }
  }
}
