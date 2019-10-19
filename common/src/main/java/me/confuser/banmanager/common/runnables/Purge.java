package me.confuser.banmanager.common.runnables;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.CleanUp;

import java.sql.SQLException;
import java.util.HashMap;

public class Purge implements Runnable {

  private BanManagerPlugin plugin;

  public Purge(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    HashMap<String, CleanUp> cleanUps = plugin.getConfig().getCleanUps();

    try {
      plugin.getPlayerKickStorage().purge(cleanUps.get("kicks"));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      plugin.getPlayerBanRecordStorage().purge(cleanUps.get("banRecords"));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      plugin.getIpBanRecordStorage().purge(cleanUps.get("ipBanRecords"));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      plugin.getPlayerMuteRecordStorage().purge(cleanUps.get("muteRecords"));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      plugin.getPlayerWarnStorage().purge(cleanUps.get("readWarnings"), true);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      plugin.getPlayerWarnStorage().purge(cleanUps.get("unreadWarnings"), false);
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }
}
