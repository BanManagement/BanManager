package me.confuser.banmanager.configs;

import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;

import com.j256.ormlite.table.DatabaseTableConfig;
import lombok.Getter;

public abstract class DatabaseConfig {

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
      private final boolean isEnabled;
      @Getter
      private final int maxConnections;
      private HashMap<String, DatabaseTableConfig<?>> tables = new HashMap<>();

      public DatabaseConfig(ConfigurationSection conf) {
            host = conf.getString("host");
            port = conf.getInt("port", 3306);
            name = conf.getString("name");
            user = conf.getString("user");
            password = conf.getString("password");
            isEnabled = conf.getBoolean("enabled");
            maxConnections = conf.getInt("maxConnections", 10);
      }

      public String getJDBCUrl() {
            return "jdbc:mysql://" + host + ":" + port + "/" + name + "?autoReconnect=true&failOverReadOnly=false&maxReconnects=10&useUnicode=true&characterEncoding=utf-8";
      }

      public DatabaseTableConfig<?> getTable(String table) {
            return tables.get(table);
      }

      public void addTable(String key, DatabaseTableConfig<?> config) {
            tables.put(key, config);
      }
}
