package me.confuser.banmanager.runnables;

import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.storage.*;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class ExpiresSync implements Runnable {

  private BanManager plugin = BanManager.getPlugin();
  private PlayerBanStorage banStorage = plugin.getPlayerBanStorage();
  private PlayerBanRecordStorage banRecordStorage = plugin.getPlayerBanRecordStorage();
  private PlayerMuteStorage muteStorage = plugin.getPlayerMuteStorage();
  private PlayerMuteRecordStorage muteRecordStorage = plugin.getPlayerMuteRecordStorage();
  private IpBanStorage ipBanStorage = plugin.getIpBanStorage();
  private IpBanRecordStorage ipBanRecordStorage = plugin.getIpBanRecordStorage();
  @Getter
  private boolean isRunning = false;

  @Override
  public void run() {
    isRunning = true;
    long now = (System.currentTimeMillis() / 1000L) + DateUtils.getTimeDiff();
    String nowStr = Long.toString(now);
    String console = plugin.getPlayerStorage().getConsole().getUUID().toString().replace("-", "");

    final String banExpireSQL = "INSERT INTO " + banRecordStorage.getTableInfo()
                                                                 .getTableName() + " (player_id, reason, expired, actor_id, pastActor_id, pastCreated, created) SELECT b.player_id, b.reason, b.expires, b.actor_id, UNHEX(?), b.created, ? FROM " + banStorage
            .getTableInfo().getTableName() + " b WHERE b.expires != '0' AND b.expires < ?";

    try {
      banStorage.executeRaw(banExpireSQL, console, nowStr, nowStr);
      banStorage.executeRaw("DELETE FROM " + banStorage.getTableInfo()
                                                       .getTableName() + " WHERE expires != 0 AND expires < ?", nowStr);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    final String muteExpireSQL = "INSERT INTO " + muteRecordStorage.getTableInfo()
                                                                   .getTableName() + " (player_id, reason, expired, actor_id, pastActor_id, pastCreated, created) SELECT b.player_id, b.reason, b.expires, b.actor_id, UNHEX(?), b.created, ? FROM " + muteStorage
            .getTableInfo().getTableName() + " b WHERE b.expires != '0' AND b.expires < ?";

    try {
      muteStorage.executeRaw(muteExpireSQL, console, nowStr, nowStr);
      muteStorage.executeRaw("DELETE FROM " + muteStorage.getTableInfo()
                                                         .getTableName() + " WHERE expires != 0 AND expires < ?", nowStr);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    final String ipExpireSQL = "INSERT INTO " + ipBanRecordStorage.getTableInfo()
                                                                  .getTableName() + " (ip, reason, expired, actor_id, pastActor_id, pastCreated, created) SELECT b.ip, b.reason, b.expires, b.actor_id, UNHEX(?), b.created, ? FROM " + ipBanStorage
            .getTableInfo().getTableName() + " b WHERE b.expires != '0' AND b.expires < ?";

    try {
      ipBanStorage.executeRaw(ipExpireSQL, console, nowStr, nowStr);
      ipBanStorage.executeRaw("DELETE FROM " + ipBanStorage.getTableInfo()
                                                           .getTableName() + " WHERE expires != 0 AND expires < ?", nowStr);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    isRunning = false;
  }
}
