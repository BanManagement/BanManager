package me.confuser.banmanager.fabric.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonMuteListener;
import me.confuser.banmanager.fabric.BanManagerEvents;
import me.confuser.banmanager.common.data.*;

public class MuteListener {

  private final CommonMuteListener listener;

  public MuteListener(BanManagerPlugin plugin) {
    this.listener = new CommonMuteListener(plugin);

    BanManagerEvents.PLAYER_MUTED_EVENT.register(this::notifyOnMute);
    BanManagerEvents.IP_MUTED_EVENT.register(this::notifyOnIpMute);
  }

  private void notifyOnMute(PlayerMuteData muteData, boolean silent) {
    listener.notifyOnMute(muteData, silent);
  }

  private void notifyOnIpMute(IpMuteData muteData, boolean silent) {
    listener.notifyOnMute(muteData, silent);
  }
}
