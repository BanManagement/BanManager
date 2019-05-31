package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateListener extends Listeners<BanManager> {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!event.getPlayer().hasPermission("bm.notify.update")) return;

    Message.get("update.notify").sendTo(event.getPlayer());
    event.getPlayer().sendMessage(ChatColor.GOLD + "http://dev.bukkit.org/bukkit-plugins/ban-management/");
  }
}
