package me.confuserr.banmanager.configs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.confuserr.banmanager.BanManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	private File file;
	private FileConfiguration config = null;

	public Config(File file) {
		this.file = file;
		
		saveDefaultConfig();
	}

	public void saveConfig() {
		try {
			getConfig().save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getConfig() {
		if (config == null)
			reloadConfig();

		return config;
	}

	public void reloadConfig() {
		config = YamlConfiguration.loadConfiguration(file);

		// Look for defaults in the jar
		InputStream defConfigStream = BanManager.getPlugin().getResource(file.getName());
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}
	}

	public void saveDefaultConfig() {
		if (!file.exists()) {
			BanManager.getPlugin().saveResource(file.getName(), false);
		}
	}

}
