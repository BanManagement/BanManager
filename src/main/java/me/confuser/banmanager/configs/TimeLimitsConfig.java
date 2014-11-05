package me.confuser.banmanager.configs;

import java.util.HashMap;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.util.DateUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class TimeLimitsConfig {
	private HashMap<String, HashMap<String, String>> limits;

	public TimeLimitsConfig(ConfigurationSection config) {
		limits = new HashMap<>();

		for (TimeLimitType type : TimeLimitType.values()) {
			ConfigurationSection typeSection = config.getConfigurationSection(type.getName());
			HashMap<String, String> groups = new HashMap<>();

			for (String name : typeSection.getKeys(false)) {
				String time = typeSection.getString(name);

				try {
					DateUtils.parseDateDiff(time, true);
				} catch (Exception e) {
					BanManager.getPlugin().getLogger().warning("Ignored " + type.getName() + " " + name + " due to invalid time");
					continue;
				}

				groups.put(name, time);
			}

			limits.put(type.getName(), groups);
		}
	}

	public boolean isPastLimit(CommandSender sender, TimeLimitType type, long expires) {
		if (sender.hasPermission("bm.timelimit." + type.getName() + ".bypass")) {
			return false;
		}

		HashMap<String, String> groups = limits.get(type);

		for (String group : groups.keySet()) {
			if (sender.hasPermission("bm.timelimit." + type.getName() + "." + group)) {
				try {
					if (expires > DateUtils.parseDateDiff(groups.get(group), true)) {
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return false;
	}
}
