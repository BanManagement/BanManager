package me.confuser.banmanager.configs;

import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;

import com.j256.ormlite.table.DatabaseTableConfig;

public abstract class DatabaseConfig {

      private final String host;
      private final int port;
      private final String name;
      private final String user;
      private final String password;
      private final boolean isEnabled;
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

      public String getHost() {
            return host;
      }

      public int getPort() {
            return port;
      }

      public String getName() {
            return name;
      }

      public String getUser() {
            return user;
      }

      public String getPassword() {
            return password;
      }

      public boolean isEnabled() {
            return isEnabled;
      }

      public int getMaxConnections() {
            return maxConnections;
      }
}
