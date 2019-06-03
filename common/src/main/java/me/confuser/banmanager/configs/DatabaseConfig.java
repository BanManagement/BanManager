package me.confuser.banmanager.configs;

import com.j256.ormlite.table.DatabaseTableConfig;
import lombok.Getter;
import me.confuser.banmanager.common.config.ConfigKeys;

import java.util.HashMap;
import java.util.Map;

import static me.confuser.banmanager.BanManager.plugin;

public abstract class DatabaseConfig {

  enum Type {
    LOCAL,
    GLOBAL
  }

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
  private HashMap<String, DatabaseTableConfig<?>> tables = new HashMap<>();

  private DatabaseConfig(Type type) {
    storageType = plugin.getConfiguration().get(type == Type.LOCAL ? ConfigKeys.DATABASE_LOCAL_STORAGETYPE : ConfigKeys.DATABASE_GLOBAL_STORAGETYPE);
    host = plugin.getConfiguration().get(type == Type.LOCAL ? ConfigKeys.DATABASE_LOCAL_HOST : ConfigKeys.DATABASE_GLOBAL_HOST);
    port = plugin.getConfiguration().get(type == Type.LOCAL ? ConfigKeys.DATABASE_LOCAL_PORT : ConfigKeys.DATABASE_GLOBAL_PORT);
    name = plugin.getConfiguration().get(type == Type.LOCAL ? ConfigKeys.DATABASE_LOCAL_NAME : ConfigKeys.DATABASE_GLOBAL_NAME);
    user = plugin.getConfiguration().get(type == Type.LOCAL ? ConfigKeys.DATABASE_LOCAL_USER : ConfigKeys.DATABASE_GLOBAL_USER);
    password = plugin.getConfiguration().get(type == Type.LOCAL ? ConfigKeys.DATABASE_LOCAL_PASSWORD : ConfigKeys.DATABASE_GLOBAL_PASSWORD);
    isEnabled = plugin.getConfiguration().get(type == Type.LOCAL ? ConfigKeys.DATABASE_LOCAL_ENABLED : ConfigKeys.DATABASE_GLOBAL_ENABLED);
    maxConnections = plugin.getConfiguration().get(type == Type.LOCAL ? ConfigKeys.DATABASE_LOCAL_MAX_CONNECTIONS : ConfigKeys.DATABASE_GLOBAL_MAX_CONNECTIONS);
    leakDetection = plugin.getConfiguration().get(type == Type.LOCAL ? ConfigKeys.DATABASE_LOCAL_LEAK_DETECTION : ConfigKeys.DATABASE_GLOBAL_LEAK_DETECTION);
    useSSL = plugin.getConfiguration().get(type == Type.LOCAL ? ConfigKeys.DATABASE_LOCAL_USE_SSL : ConfigKeys.DATABASE_GLOBAL_USE_SSL);
    verifyServerCertificate = plugin.getConfiguration().get(type == Type.LOCAL ? ConfigKeys.DATABASE_LOCAL_VERIFY_SERVER_CERT : ConfigKeys.DATABASE_GLOBAL_VERIFY_SERVER_CERT);

    if (maxConnections > 30) maxConnections = 30;
  }

  DatabaseConfig(Type type, HashMap<String, Class> types) {
    this(type);

    //for (Map.Entry<String, Class> entry : types.entrySet()) {
    //  addTable(entry.getKey(), new DatabaseTableConfig<>(entry.getValue(), plugin.getConfiguration().getString("tables." + entry.getKey()), null));
    //}

    for (Map.Entry<String, Class> entry : types.entrySet()) {
      addTable(entry.getKey(), new DatabaseTableConfig<>(entry.getValue(), plugin.getConfiguration().get(type == Type.LOCAL ? ConfigKeys.DATABASE_LOCAL_TABLES : ConfigKeys.DATABASE_GLOBAL_TABLES).get(entry.getKey()), null));
    }

  }

  public String getJDBCUrl() {
    String url = "jdbc:" + storageType + "://" + host + ":" + port + "/" + name +
            "?autoReconnect=true&failOverReadOnly=false&maxReconnects=10&useUnicode=true&characterEncoding=utf-8" +
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