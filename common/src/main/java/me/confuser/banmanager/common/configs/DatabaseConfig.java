package me.confuser.banmanager.common.configs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public abstract class DatabaseConfig {

  @Getter
  private final String storageType;
  @Getter
  private final String host;
  @Getter
  private final int port;
  @Getter
  private final String name;
  @Getter
  private final String user;
  @Getter
  private final String password;
  @Getter
  private final boolean useSSL;
  @Getter
  private final boolean verifyServerCertificate;
  @Getter
  private final boolean allowPublicKeyRetrieval;
  @Getter
  private final boolean isEnabled;
  @Getter
  private int maxConnections;
  @Getter
  private int leakDetection;
  @Getter
  private int maxLifetime;
  @Getter
  private int connectionTimeout;
  @Getter
  private String instanceId;
  @Getter
  private HashMap<String, DatabaseTableConfig<?>> tables = new HashMap<>();

  private File dataFolder;

  private DatabaseConfig(File dataFolder, ConfigurationSection conf) {
    this.dataFolder = dataFolder;

    storageType = conf.getString("storageType", "mysql").toLowerCase();
    host = conf.getString("host");
    port = conf.getInt("port", 3306);
    name = conf.getString("name");
    user = conf.getString("user");
    password = conf.getString("password");
    isEnabled = conf.getBoolean("enabled", false);
    maxConnections = conf.getInt("maxConnections", 10);
    leakDetection = conf.getInt("leakDetection", 0);
    useSSL = conf.getBoolean("useSSL", false);
    allowPublicKeyRetrieval = conf.getBoolean("allowPublicKeyRetrieval", false);
    verifyServerCertificate = conf.getBoolean("verifyServerCertificate", false);
    maxLifetime = conf.getInt("maxLifetime", 1800000);
    connectionTimeout = conf.getInt("connectionTimeout", 30000);
    instanceId = conf.getString("instanceId", "");

    if (maxConnections > 30) maxConnections = 30;
  }

  DatabaseConfig(File dataFolder, ConfigurationSection conf, HashMap<String, Class> types) {
    this(dataFolder, conf);

    for (Map.Entry<String, Class> entry : types.entrySet()) {
      addTable(entry.getKey(), new DatabaseTableConfig<>(entry.getValue(), conf
          .getString("tables." + entry.getKey()), null));
    }
  }

  public String getJDBCUrl() {
    if (storageType.equals("h2")) {
      return "jdbc:h2:file:" + new File(dataFolder, name).getAbsolutePath()
          + ";mode=MySQL;DB_CLOSE_ON_EXIT=TRUE;FILE_LOCK=NO;IGNORECASE=TRUE";
    }

    if (storageType.equals("mariadb")) {
      // mariadb-java-client 3.x rejects most legacy mysql parameters with a WARN log.
      // Only emit options the driver actually supports and translate useSSL +
      // verifyServerCertificate into the modern sslMode setting.
      String sslMode;
      if (!useSSL) {
        sslMode = "disable";
      } else if (verifyServerCertificate) {
        sslMode = "verify-full";
      } else {
        sslMode = "trust";
      }
      return "jdbc:mariadb://" + host + ":" + port + "/" + name
          + "?useServerPrepStmts=true"
          + "&cachePrepStmts=true"
          + "&prepStmtCacheSize=250"
          + "&prepStmtCacheSqlLimit=2048"
          + "&sslMode=" + sslMode;
    }

    // mysql via mysql-connector-j 8.x. mariadb-java-client 3.x no longer hijacks
    // jdbc:mysql:// URLs, so the legacy "disableMariaDbDriver" hint is gone.
    return "jdbc:mysql://" + host + ":" + port + "/" + name
        + "?autoReconnect=true&failOverReadOnly=false&maxReconnects=10"
        + "&useUnicode=true&characterEncoding=utf-8"
        + "&serverTimezone=UTC"
        + "&useSSL=" + useSSL
        + "&allowPublicKeyRetrieval=" + allowPublicKeyRetrieval
        + "&verifyServerCertificate=" + verifyServerCertificate;
  }

  public DatabaseTableConfig<?> getTable(String table) {
    return tables.get(table);
  }

  // Used by ReportsLogger
  public void addTable(String key, DatabaseTableConfig<?> config) {
    tables.put(key, config);
  }

  /**
   * Returns the SQL expression for the current unix timestamp in seconds.
   * This expression can be embedded directly in INSERT/UPDATE statements.
   * Works with MySQL, MariaDB, and H2 (in MySQL compatibility mode).
   *
   * @return SQL expression that evaluates to current unix timestamp
   */
  public String getTimestampNow() {
    return "UNIX_TIMESTAMP()";
  }

  /**
   * Returns the SQL query to fetch the current unix timestamp.
   * Works with MySQL, MariaDB, and H2 (in MySQL compatibility mode).
   *
   * @return SQL query that returns the current unix timestamp
   */
  public String getTimestampQuery() {
    return "SELECT UNIX_TIMESTAMP()";
  }
}
