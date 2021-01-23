package me.confuser.banmanager.common.configs;

import com.j256.ormlite.table.DatabaseTableConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.confuser.banmanager.common.configuration.ConfigurationSection;

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
  private HashMap<String, DatabaseTableConfig<?>> tables = new HashMap<>();

  private File dataFolder;

  private DatabaseConfig(File dataFolder, ConfigurationSection conf) {
    this.dataFolder = dataFolder;

    storageType = conf.getString("storageType", "mysql");
    host = conf.getString("host");
    port = conf.getInt("port", 3306);
    name = conf.getString("name");
    user = conf.getString("user");
    password = conf.getString("password");
    isEnabled = conf.getBoolean("enabled");
    maxConnections = conf.getInt("maxConnections", 10);
    leakDetection = conf.getInt("leakDetection", 0);
    useSSL = conf.getBoolean("useSSL", false);
    verifyServerCertificate = conf.getBoolean("verifyServerCertificate", false);
    maxLifetime = conf.getInt("maxLifetime", 1800000);
    connectionTimeout = conf.getInt("connectionTimeout", 30000);

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
    if (storageType.equals("h2")) return "jdbc:h2:file:" + new File(dataFolder, name).getAbsolutePath() + ";mode=MySQL;DB_CLOSE_ON_EXIT=TRUE;FILE_LOCK=NO";

    String url = "jdbc:" + storageType + "://" + host + ":" + port + "/" + name +
        "?autoReconnect=true&failOverReadOnly=false&maxReconnects=10&useUnicode=true&characterEncoding=utf-8" +
        "&serverTimezone=UTC" +
        "&useSSL=" + useSSL +
        "&verifyServerCertificate=" + verifyServerCertificate;

    if (!storageType.equals("mariadb")) {
      url += "&disableMariaDbDriver";
    }

    return url;
  }

  public DatabaseTableConfig<?> getTable(String table) {
    return tables.get(table);
  }

  // Used by ReportsLogger
  public void addTable(String key, DatabaseTableConfig<?> config) {
    tables.put(key, config);
  }
}
