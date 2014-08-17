package me.confuser.banmanager.configs;

import java.util.HashSet;

import org.bukkit.command.PluginCommand;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.configs.Config;

public class DefaultConfig extends Config<BanManager> {
	private DatabaseConfig localDb;
	private DatabaseConfig externalDb;
	private HashSet<String> mutedBlacklistCommands = new HashSet<String>();
	private boolean dupeIpCheck = true;
	private boolean logKicks = false;

	public DefaultConfig() {
		super("config.yml");
	}

	@Override
	public void afterLoad() {
		localDb = new LocalDatabaseConfig(conf.getConfigurationSection("databases.local"));
		//externalDb = new DatabaseConfig(conf.getConfigurationSection("databases.external"));
		
		for (String cmd : conf.getStringList("mutedCommandBlacklist")) {
			mutedBlacklistCommands.add(cmd);
			
			// Check for aliases
			PluginCommand command = plugin.getCommand(cmd);
			
			if (command == null)
				continue;
			
			for (String aliasCmd : command.getAliases()) {
				mutedBlacklistCommands.add(aliasCmd);
			}
		}
		
		dupeIpCheck = conf.getBoolean("duplicateIpCheck", true);
		logKicks = conf.getBoolean("logKicks", false);
	}
	
	public DatabaseConfig getLocalDb() {
		return localDb;
	}
	
	public DatabaseConfig getExternalDb() {
		return externalDb;
	}

	@Override
	public void onSave() {
		
	}

	public boolean isBlockedCommand(String cmd) {
		return mutedBlacklistCommands.contains(cmd);
	}

	public boolean isDuplicateIpCheckEnabled() {
		return dupeIpCheck;
	}

	public boolean isKickLoggingEnabled() {
		return logKicks;
	}

}
