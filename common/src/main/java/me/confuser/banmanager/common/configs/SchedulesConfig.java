package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.CommonLogger;

import java.io.File;
import java.util.HashMap;

public class SchedulesConfig extends Config {

  private HashMap<String, Integer> schedules = new HashMap<>(6);
  private HashMap<String, Long> lastChecked = new HashMap<>(4);

  public SchedulesConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "schedules.yml", logger);
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
      schedules.put(key, conf.getInt(("scheduler." + key), 0));
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
