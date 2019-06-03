package me.confuser.banmanager.configs;

import com.j256.ormlite.table.DatabaseTableConfig;

import java.util.HashMap;
import java.util.Map;

public abstract class DatabaseConfig {

  private DatabaseConfig(ConfigurationSection conf) {
    if (maxConnections > 30)
      maxConnections = 30;
  }

  DatabaseConfig(ConfigurationSection conf, HashMap<String, Class> types) {
    this(conf);

    for (Map.Entry<String, Class> entry : types.entrySet()) {
      addTable(entry.getKey(), new DatabaseTableConfig<>(entry.getValue(), conf
              .getString("tables." + entry.getKey()), null));
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
