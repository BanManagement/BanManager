package me.confuser.banmanager.common.runnables;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.data.global.GlobalIpBanData;
import me.confuser.banmanager.common.data.global.GlobalIpBanRecordData;
import me.confuser.banmanager.common.data.global.GlobalPlayerBanData;
import me.confuser.banmanager.common.data.global.GlobalPlayerBanRecordData;
import me.confuser.banmanager.common.data.global.GlobalPlayerMuteData;
import me.confuser.banmanager.common.data.global.GlobalPlayerMuteRecordData;
import me.confuser.banmanager.common.data.global.GlobalPlayerNoteData;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.storage.IpBanStorage;
import me.confuser.banmanager.common.storage.PlayerBanStorage;
import me.confuser.banmanager.common.storage.PlayerMuteStorage;
import me.confuser.banmanager.common.storage.PlayerNoteStorage;

import java.sql.SQLException;

/**
 * Applies global records to local storage with shared dedupe semantics for
 * both command-origin writes and sync replication.
 */
public class GlobalLocalApplyHelper {

  private final BanManagerPlugin plugin;
  private final PlayerBanStorage playerBanStorage;
  private final IpBanStorage ipBanStorage;
  private final PlayerMuteStorage playerMuteStorage;
  private final PlayerNoteStorage playerNoteStorage;

  public GlobalLocalApplyHelper(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.playerBanStorage = plugin.getPlayerBanStorage();
    this.ipBanStorage = plugin.getIpBanStorage();
    this.playerMuteStorage = plugin.getPlayerMuteStorage();
    this.playerNoteStorage = plugin.getPlayerNoteStorage();
  }

  public PlayerBanData applyBan(GlobalPlayerBanData globalBan, boolean fromSync) throws SQLException {
    PlayerBanData localBanData = globalBan.toLocal(plugin);
    PlayerBanData existingBan = playerBanStorage.retrieveBan(globalBan.getUUID());
    boolean silent = isSilentSync(fromSync);

    if (existingBan != null) {
      if (existingBan.equalsBan(localBanData)) {
        return existingBan;
      }

      if (!playerBanStorage.unban(existingBan, globalBan.getActor(plugin), "", false, silent)) {
        return null;
      }
    } else if (playerBanStorage.isBanned(globalBan.getUUID())) {
      playerBanStorage.removeBan(globalBan.getUUID());
    }

    if (!playerBanStorage.ban(localBanData, fromSync)) {
      return null;
    }

    return playerBanStorage.getBan(globalBan.getUUID());
  }

  public boolean applyUnban(GlobalPlayerBanRecordData record, boolean fromSync) throws SQLException {
    PlayerBanData ban = playerBanStorage.getBan(record.getUUID());
    if (ban == null) {
      return true;
    }

    return playerBanStorage.unban(ban, record.getActor(plugin), "", false, isSilentSync(fromSync));
  }

  public IpBanData applyIpBan(GlobalIpBanData globalBan, boolean fromSync) throws SQLException {
    IpBanData localBanData = globalBan.toLocal(plugin);
    IpBanData existingBan = ipBanStorage.retrieveBan(globalBan.getIp());
    boolean silent = isSilentSync(fromSync);

    if (existingBan != null) {
      if (existingBan.equalsBan(localBanData)) {
        return existingBan;
      }

      if (!ipBanStorage.unban(existingBan, globalBan.getActor(plugin), "", false, silent)) {
        return null;
      }
    } else if (ipBanStorage.isBanned(globalBan.getIp())) {
      ipBanStorage.removeBan(globalBan.getIp());
    }

    if (!ipBanStorage.ban(localBanData, fromSync)) {
      return null;
    }

    return ipBanStorage.getBan(globalBan.getIp());
  }

  public boolean applyIpUnban(GlobalIpBanRecordData record, boolean fromSync) throws SQLException {
    IpBanData ban = ipBanStorage.getBan(record.getIp());
    if (ban == null) {
      return true;
    }

    return ipBanStorage.unban(ban, record.getActor(plugin), "", false, isSilentSync(fromSync));
  }

  public boolean applyMute(GlobalPlayerMuteData globalMute, boolean fromSync) throws SQLException {
    PlayerMuteData localMuteData = globalMute.toLocal(plugin);
    PlayerMuteData existingMute = playerMuteStorage.retrieveMute(globalMute.getUUID());
    boolean silent = isSilentSync(fromSync);

    if (existingMute != null) {
      if (existingMute.equalsMute(localMuteData)) {
        return true;
      }

      if (!playerMuteStorage.unmute(existingMute, globalMute.getActor(plugin), "", false, silent)) {
        return false;
      }
    } else if (playerMuteStorage.isMuted(globalMute.getUUID())) {
      playerMuteStorage.removeMute(globalMute.getUUID());
    }

    return playerMuteStorage.mute(localMuteData, fromSync);
  }

  public boolean applyUnmute(GlobalPlayerMuteRecordData record, boolean fromSync) throws SQLException {
    PlayerMuteData mute = playerMuteStorage.getMute(record.getUUID());
    if (mute == null) {
      return true;
    }

    return playerMuteStorage.unmute(mute, record.getActor(plugin), "", false, isSilentSync(fromSync));
  }

  public boolean applyNote(GlobalPlayerNoteData globalNote, boolean fromSync) throws SQLException {
    PlayerNoteData localNote = globalNote.toLocal(plugin);
    CloseableIterator<PlayerNoteData> notes = null;

    try {
      notes = playerNoteStorage.getNotes(globalNote.getUUID());

      while (notes.hasNext()) {
        if (notes.next().equalsNote(localNote)) {
          return false;
        }
      }
    } finally {
      if (notes != null) notes.closeQuietly();
    }

    return playerNoteStorage.addNote(localNote, isSilentSync(fromSync));
  }

  private boolean isSilentSync(boolean fromSync) {
    return fromSync && !plugin.getConfig().isBroadcastOnSync();
  }
}
