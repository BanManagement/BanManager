package me.confuser.banmanager.runnables;

import lombok.Getter;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;

public abstract class BmRunnable implements Runnable {
  protected BanManagerPlugin plugin;

  @Getter
  protected long lastChecked = 0;
  @Getter
  protected boolean isRunning = false;
  @Getter
  protected final String name;

  public BmRunnable(BanManagerPlugin plugin, String schedulerName) {
    this.plugin = plugin;
    name = schedulerName;

    lastChecked = plugin.getSchedulesConfig().getLastChecked(name);
  }

  public boolean shouldExecute() {
    return !isRunning && (System.currentTimeMillis() / 1000L) - lastChecked > plugin.getSchedulesConfig().getSchedule(name);
  }

  public void beforeRun() {
    isRunning = true;
  }

  public void afterRun() {
    lastChecked = System.currentTimeMillis() / 1000L;
    plugin.getSchedulesConfig().setLastChecked(name, lastChecked);
    isRunning = false;
  }
}
