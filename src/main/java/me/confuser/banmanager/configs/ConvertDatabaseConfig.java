package me.confuser.banmanager.configs;

import java.util.HashMap;
import org.bukkit.configuration.ConfigurationSection;

public class ConvertDatabaseConfig extends DatabaseConfig {
	private HashMap<String, String> tableNames = new HashMap<String, String>();

	public ConvertDatabaseConfig(ConfigurationSection conf) {
		super(conf);

		for (String key : conf.getConfigurationSection("tables").getKeys(false)) {
			String path = "tables." + key;
			tableNames.put(key, conf.getString(path));
		}
	}

	public String getTableName(String key) {
		return tableNames.get(key);
	}

}
