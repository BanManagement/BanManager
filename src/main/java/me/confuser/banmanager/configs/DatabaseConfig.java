package me.confuser.banmanager.configs;

import java.util.HashMap;

import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;

import org.bukkit.configuration.ConfigurationSection;

import com.j256.ormlite.table.DatabaseTableConfig;

public class DatabaseConfig {
	private final String host;
	private final int port;
	private final String name;
	private final String user;
	private final String password;
	private final boolean isEnabled;
	private HashMap<String, DatabaseTableConfig<?>> tables = new HashMap<String, DatabaseTableConfig<?>>();
	
	public DatabaseConfig(ConfigurationSection conf) {
		host = conf.getString("host");
		port = conf.getInt("port");
		name = conf.getString("name");
		user = conf.getString("user");
		password = conf.getString("password");
		isEnabled = conf.getBoolean("enabled");
		
		DatabaseTableConfig<PlayerData> playerTable = new DatabaseTableConfig<PlayerData>();
		playerTable.setTableName(conf.getString("tables.players"));
		tables.put("players", playerTable);
		
		DatabaseTableConfig<PlayerBanData> banTable = new DatabaseTableConfig<PlayerBanData>();
		banTable.setTableName(conf.getString("tables.bans"));
		tables.put("bans", banTable);
	}
	
	public String getJDBCUrl() {
		return "jdbc:mysql://" + host + ":" + port + "/" + name + "?autoReconnect=true&failOverReadOnly=false&maxReconnects=10&useUnicode=true&characterEncoding=utf-8";
	}
	
	public DatabaseTableConfig<?> getTable(String table) {
		return tables.get(table);
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
}
