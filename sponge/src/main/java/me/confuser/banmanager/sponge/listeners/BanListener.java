package me.confuser.banmanager.sponge.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.listeners.CommonBanListener;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.sponge.api.events.IpBannedEvent;
import me.confuser.banmanager.sponge.api.events.IpRangeBannedEvent;
import me.confuser.banmanager.sponge.api.events.NameBannedEvent;
import me.confuser.banmanager.sponge.api.events.PlayerBannedEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

import java.util.List;

public class BanListener {
  private final CommonBanListener listener;

  public BanListener(BanManagerPlugin plugin) {
    this.listener = new CommonBanListener(plugin);
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnBan(PlayerBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnIpBan(IpBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnIpRangeBan(IpRangeBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnNameBan(NameBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }
}
