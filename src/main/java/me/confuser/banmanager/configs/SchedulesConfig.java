package me.confuser.banmanager.configs;

import java.util.HashMap;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.configs.Config;

public class SchedulesConfig extends Config<BanManager> {
	private HashMap<String, Integer> schedules = new HashMap<>(6);
	private HashMap<String, Long> lastChecked = new HashMap<>(4);

	public SchedulesConfig() {
		super("schedules.yml");
	}

	public int getSchedule(String schedule) {
		return schedules.get(schedule);
	}

	public long getLastChecked(String lastChecked) {
		return this.lastChecked.get(lastChecked);
	}
	
	public void setLastChecked(String key, long value) {
		lastChecked.put(key, value);
	}

	@Override
	public void afterLoad() {
		for (String key : conf.getConfigurationSection("scheduler").getKeys(false)) {
			String path = "scheduler." + key;
			schedules.put(key, conf.getInt(path, 0) * 20);
		}

		for (String key : conf.getConfigurationSection("lastChecked").getKeys(false)) {
			String path = "lastChecked." + key;
			lastChecked.put(key, conf.getLong(path, 0));
		}

	}

	@Override
	public void onSave() {
		if (lastChecked.size() > 0)
			conf.set("lastChecked", lastChecked);
	}

}
