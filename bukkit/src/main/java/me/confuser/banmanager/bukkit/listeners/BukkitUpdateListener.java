package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.sender.Sender;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class BukkitUpdateListener implements Listener {

  private final BMBukkitPlugin plugin;

  public BukkitUpdateListener(BMBukkitPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!event.getPlayer().hasPermission("bm.notify.update")) return;

    Sender wrap = plugin.getSenderFactory().wrap(event.getPlayer());
    Message.UPDATE_NOTIFY.send(wrap);

    event.getPlayer().sendMessage(ChatColor.GOLD + "http://dev.bukkit.org/bukkit-plugins/ban-management/");
  }
}
