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
	private boolean debug = false;

	public DefaultConfig() {
		super("config.yml");
	}

	@Override
	public void afterLoad() {
		localDb = new LocalDatabaseConfig(conf.getConfigurationSection("databases.local"));
		//externalDb = new DatabaseConfig(conf.getConfigurationSection("databases.external"));
		dupeIpCheck = conf.getBoolean("duplicateIpCheck", true);
		logKicks = conf.getBoolean("logKicks", false);
		debug = conf.getBoolean("debug", false);
		
		// Run this after startup to ensure all aliases are found
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				plugin.getLogger().info("The following commands are blocked whilst muted:");
				for (String cmd : conf.getStringList("mutedCommandBlacklist")) {
					mutedBlacklistCommands.add(cmd);
					// TODO Use a stringbuilder
					String info = cmd;
					
					// Check for aliases
					PluginCommand command = plugin.getServer().getPluginCommand(cmd);
					if (command == null) {
						plugin.getLogger().info(info);
						continue;
					}
					
					info += " - ";
					
					for (String aliasCmd : command.getAliases()) {
						info += aliasCmd + " ";
						mutedBlacklistCommands.add(aliasCmd);
					}
					
					plugin.getLogger().info(info);
				}
			}
			
		});
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
	
	public boolean isDebugEnabled() {
		return debug;
	}

}
