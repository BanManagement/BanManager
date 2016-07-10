package me.confuser.banmanager.runnables;

import lombok.Getter;
import me.confuser.banmanager.BanManager;

public abstract class BmRunnable implements Runnable {
  protected BanManager plugin = BanManager.getPlugin();

  @Getter
  protected long lastChecked = 0;
  @Getter
  protected boolean isRunning = false;
  @Getter
  protected final String name;

  public BmRunnable(String schedulerName) {
    name = schedulerName;

    lastChecked = plugin.getSchedulesConfig().getLastChecked(name);
  }

  public boolean shouldExecute() {
    return (System.currentTimeMillis() / 1000L) - lastChecked > plugin.getSchedulesConfig().getSchedule(name);
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
