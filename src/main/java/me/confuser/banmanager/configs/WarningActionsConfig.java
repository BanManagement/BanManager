package me.confuser.banmanager.configs;

import java.util.HashMap;

import lombok.Getter;
import me.confuser.banmanager.BanManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.configuration.ConfigurationSection;

public class WarningActionsConfig {
	private BanManager plugin = BanManager.getPlugin();
	@Getter
	private boolean isEnabled = false;
	private HashMap<Integer, String> actions;

	public WarningActionsConfig(ConfigurationSection config) {
		isEnabled = config.getBoolean("enabled", false);
		actions = new HashMap<>();
		
		for (String amount : config.getConfigurationSection("actions").getKeys(false)) {
			if (!StringUtils.isNumeric(amount)) {
				plugin.getLogger().warning("Invalid warning action, " + amount + " is not numeric");
				continue;
			}
			
			actions.put(NumberUtils.toInt(amount), config.getString("actions." + amount));
		}
	}
	
	public String getCommand(int amount) {
		return actions.get(amount);
	}

}
