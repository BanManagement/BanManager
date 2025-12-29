package me.confuser.banmanager.velocity.listeners;


import com.velocitypowered.api.event.Subscribe;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.velocity.api.events.IpBannedEvent;
import me.confuser.banmanager.velocity.api.events.IpRangeBannedEvent;
import me.confuser.banmanager.velocity.api.events.NameBannedEvent;
import me.confuser.banmanager.velocity.api.events.PlayerBannedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonBanListener;

import com.velocitypowered.api.event.PostOrder;

public class BanListener extends Listener {

  private final CommonBanListener listener;

  public BanListener(BanManagerPlugin plugin) {
    this.listener = new CommonBanListener(plugin);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void notifyOnBan(PlayerBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }

  @Subscribe(order = PostOrder.FIRST)
  public void notifyOnIpBan(IpBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }

  @Subscribe(order = PostOrder.FIRST)
  public void notifyOnIpRangeBan(IpRangeBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }

  @Subscribe(order = PostOrder.FIRST)
  public void notifyOnNameBan(NameBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }
}
