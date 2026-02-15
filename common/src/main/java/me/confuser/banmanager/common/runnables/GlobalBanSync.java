package me.confuser.banmanager.common.runnables;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.global.GlobalPlayerBanData;
import me.confuser.banmanager.common.data.global.GlobalPlayerBanRecordData;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.storage.PlayerBanStorage;
import me.confuser.banmanager.common.storage.global.GlobalPlayerBanRecordStorage;
import me.confuser.banmanager.common.storage.global.GlobalPlayerBanStorage;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class GlobalBanSync extends BmRunnable {

  private GlobalPlayerBanStorage banStorage;
  private PlayerBanStorage localBanStorage;
  private GlobalPlayerBanRecordStorage recordStorage;
  private GlobalLocalApplyHelper applyHelper;

  public GlobalBanSync(BanManagerPlugin plugin) {
    super(plugin, "externalPlayerBans");

    banStorage = plugin.getGlobalPlayerBanStorage();
    localBanStorage = plugin.getPlayerBanStorage();
    recordStorage = plugin.getGlobalPlayerBanRecordStorage();
    applyHelper = new GlobalLocalApplyHelper(plugin);
  }

  @Override
  protected DatabaseConfig getCheckpointDbConfig() {
    return plugin.getConfig().getGlobalDb();
  }

  @Override
  protected BaseDaoImpl<?, ?> getCheckpointDao() {
    return banStorage;
  }

  @Override
  public void run() {
    newBans();
    newUnbans();
  }

  private void newBans() {
    CloseableIterator<GlobalPlayerBanData> itr = null;

    try {
      itr = banStorage.findBans(lastChecked);

      while (itr.hasNext()) {
        GlobalPlayerBanData ban = itr.next();

        final PlayerBanData globalBan = applyHelper.applyBan(ban, true);
        if (globalBan == null) continue;

        kickPlayer(globalBan);

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }
  }

  private void kickPlayer(PlayerBanData globalBan) {
    plugin.getScheduler().runSync(() -> {
      CommonPlayer bukkitPlayer = plugin.getServer().getPlayer(globalBan.getPlayer().getUUID());

      if (bukkitPlayer == null || !bukkitPlayer.isOnline()) return;

      Message kickMessage;

      if (globalBan.getExpires() == 0) {
        kickMessage = Message.get("ban.player.kick");
      } else {
        kickMessage = Message.get("tempban.player.kick");
        kickMessage.set("expires", DateUtils.getDifferenceFormat(globalBan.getExpires()));
      }

      kickMessage
        .set("displayName", bukkitPlayer.getDisplayName())
        .set("player", globalBan.getPlayer().getName())
        .set("reason", globalBan.getReason())
        .set("actor", globalBan.getActor().getName());

      bukkitPlayer.kick(kickMessage.toString());
    });
  }

  private void newUnbans() {
    CloseableIterator<GlobalPlayerBanRecordData> itr = null;

    try {
      itr = recordStorage.findUnbans(lastChecked);

      while (itr.hasNext()) {
        GlobalPlayerBanRecordData record = itr.next();

        applyHelper.applyUnban(record, true);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }
  }
}
