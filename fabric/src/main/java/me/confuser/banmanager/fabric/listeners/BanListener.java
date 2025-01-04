package me.confuser.banmanager.fabric.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonBanListener;
import me.confuser.banmanager.fabric.BanManagerEvents;
import me.confuser.banmanager.common.data.*;

public class BanListener {

  private final CommonBanListener listener;

  public BanListener(BanManagerPlugin plugin) {
    this.listener = new CommonBanListener(plugin);

    BanManagerEvents.PLAYER_BANNED_EVENT.register(this::notifyOnBan);
    BanManagerEvents.IP_BANNED_EVENT.register(this::notifyOnIpBan);
    BanManagerEvents.IP_RANGE_BANNED_EVENT.register(this::notifyOnIpRangeBan);
    BanManagerEvents.NAME_BANNED_EVENT.register(this::notifyOnNameBan);
  }

  private void notifyOnBan(PlayerBanData banData, boolean silent) {
    listener.notifyOnBan(banData, silent);
  }

  private void notifyOnIpBan(IpBanData banData, boolean silent) {
    listener.notifyOnBan(banData, silent);
  }

  private void notifyOnIpRangeBan(IpRangeBanData banData, boolean silent) {
    listener.notifyOnBan(banData, silent);
  }

  private void notifyOnNameBan(NameBanData banData, boolean silent) {
    listener.notifyOnBan(banData, silent);
  }
}
