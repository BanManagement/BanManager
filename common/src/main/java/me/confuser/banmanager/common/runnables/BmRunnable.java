package me.confuser.banmanager.common.runnables;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.dao.GenericRawResults;

import java.sql.SQLException;


public abstract class BmRunnable implements Runnable {

  @Getter
  protected final String name;
  protected BanManagerPlugin plugin;
  @Getter
  protected long lastChecked = 0;
  @Getter
  protected long runCheckpoint = 0;
  @Getter
  protected long lastRunLocal = 0;
  @Getter
  protected boolean isRunning = false;

  public BmRunnable(BanManagerPlugin plugin, String schedulerName) {
    this.plugin = plugin;
    name = schedulerName;

    lastChecked = plugin.getSchedulesConfig().getLastChecked(name);
    lastRunLocal = plugin.getSchedulesConfig().getLastRunLocal(name);
  }

  public boolean shouldExecute() {
    int scheduleSeconds = plugin.getSchedulesConfig().getSchedule(name);
    if (scheduleSeconds <= 0) {
      return false;
    }
    return !isRunning && (System.currentTimeMillis() / 1000L) - lastRunLocal > scheduleSeconds;
  }

  public void beforeRun() {
    isRunning = true;
    runCheckpoint = fetchDbTime();
  }

  public void afterRun() {
    if (runCheckpoint > 0) {
      lastChecked = runCheckpoint;
    } else {
      lastChecked = System.currentTimeMillis() / 1000L;
    }
    plugin.getSchedulesConfig().setLastChecked(name, lastChecked);

    lastRunLocal = System.currentTimeMillis() / 1000L;
    plugin.getSchedulesConfig().setLastRunLocal(name, lastRunLocal);

    isRunning = false;
  }

  protected long fetchDbTime() {
    try {
      String query = getCheckpointDbConfig().getTimestampQuery();
      GenericRawResults<String[]> results = getCheckpointDao().queryRaw(query);
      String[] firstRow = results.getFirstResult();
      try {
        results.close();
      } catch (Exception ignored) {
        // Ignore close errors
      }

      if (firstRow == null || firstRow.length == 0 || firstRow[0] == null) {
        plugin.getLogger().warning("DB time query returned no results, falling back to local time");
        return 0;
      }

      String result = firstRow[0];

      // Handle decimal results (some drivers like MariaDB return 0.0)
      if (result.contains(".")) {
        return Double.valueOf(result).longValue();
      } else {
        return Long.parseLong(result);
      }
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to fetch DB time for sync checkpoint: " + e.getMessage());
      return 0;
    }
  }

  protected DatabaseConfig getCheckpointDbConfig() {
    return plugin.getConfig().getLocalDb();
  }

  protected BaseDaoImpl<?, ?> getCheckpointDao() {
    return plugin.getPlayerStorage();
  }
}
