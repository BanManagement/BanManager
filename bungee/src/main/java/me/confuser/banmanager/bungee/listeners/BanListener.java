package me.confuser.banmanager.bungee.listeners;


import me.confuser.banmanager.bungee.api.events.IpBannedEvent;
import me.confuser.banmanager.bungee.api.events.IpRangeBannedEvent;
import me.confuser.banmanager.bungee.api.events.NameBannedEvent;
import me.confuser.banmanager.bungee.api.events.PlayerBannedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonBanListener;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BanListener implements Listener {

  private final CommonBanListener listener;

  public BanListener(BanManagerPlugin plugin) {
    this.listener = new CommonBanListener(plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnBan(PlayerBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnIpBan(IpBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnIpRangeBan(IpRangeBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnNameBan(NameBannedEvent event) {
    listener.notifyOnBan(event.getBan(), event.isSilent());
  }
}
