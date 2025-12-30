package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.CommonLogger;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class SchedulesConfig extends Config {

  private ConcurrentHashMap<String, Integer> schedules = new ConcurrentHashMap<>(13);
  private ConcurrentHashMap<String, Long> lastChecked = new ConcurrentHashMap<>(12);
  private ConcurrentHashMap<String, Long> lastRunLocal = new ConcurrentHashMap<>(12);

  public SchedulesConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "schedules.yml", logger);
  }

  public int getSchedule(String scheduleName) {
    Integer schedule = schedules.get(scheduleName);

    if (schedule == null) {
      logger.severe("Unknown schedule " + scheduleName + ", defaulting to 30 seconds");

      return 30;
    }

    return schedule;
  }

  public long getLastChecked(String lastChecked) {
    Long checked = this.lastChecked.get(lastChecked);

    if (checked == null) {
      logger.severe("Unknown last checked " + lastChecked + ", defaulting to 0");
      return 0;
    }

    return checked;
  }

  public void setLastChecked(String key, long value) {
    lastChecked.put(key, value);
  }

  public long getLastRunLocal(String taskName) {
    Long runTime = lastRunLocal.get(taskName);
    return runTime != null ? runTime : 0;
  }

  public void setLastRunLocal(String key, long value) {
    lastRunLocal.put(key, value);
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
