package me.confuser.banmanager.configs;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.configs.Config;

import java.util.HashMap;

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
    Long checked = this.lastChecked.get(lastChecked);

    if (checked == null) return 0;

    return checked;
  }

  public void setLastChecked(String key, long value) {
    lastChecked.put(key, value);
  }

  @Override
  public void afterLoad() {
    for (String key : conf.getConfigurationSection("scheduler").getKeys(false)) {
      schedules.put(key, (conf.getInt(("scheduler." + key), 0) * 20));
    }

    for (String key : conf.getConfigurationSection("lastChecked").getKeys(false)) {
      lastChecked.put(key, conf.getLong(("lastChecked." + key), 0));
    }

  }

  @Override
  public void onSave() {
    if (lastChecked.size() > 0) {
      conf.set("lastChecked", lastChecked);
    }
  }

}
