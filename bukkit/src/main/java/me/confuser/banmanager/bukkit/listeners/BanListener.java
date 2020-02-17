package me.confuser.banmanager.bukkit.listeners;


import me.confuser.banmanager.bukkit.api.events.IpBannedEvent;
import me.confuser.banmanager.bukkit.api.events.IpRangeBannedEvent;
import me.confuser.banmanager.bukkit.api.events.NameBannedEvent;
import me.confuser.banmanager.bukkit.api.events.PlayerBannedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.listeners.CommonBanListener;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class BanListener implements Listener {

  private final CommonBanListener listener;

  public BanListener(BanManagerPlugin plugin) {
    this.listener = new CommonBanListener(plugin);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnBan(PlayerBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnIpBan(IpBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnIpRangeBan(IpRangeBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnNameBan(NameBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }
}
