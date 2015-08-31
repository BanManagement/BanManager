package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;
import org.apache.commons.lang.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener extends Listeners<BanManager> {

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent event) {
    if (!plugin.getPlayerMuteStorage().isMuted(event.getPlayer().getUniqueId())) {
      return;
    }

    // Split the command
    String[] args = event.getMessage().split(" ", 6);

    // Get rid of the first /
    String cmd = args[0].replace("/", "").toLowerCase();

    if (!plugin.getConfiguration().isBlockedCommand(cmd)) {
      // Check if arguments blocked
      boolean shouldCancel = false;
      for (int i = 1; i < args.length; i++) {
        if (plugin.getConfiguration().isBlockedCommand(cmd + " " + StringUtils.join(args, " ", 1, i + 1))) {
          shouldCancel = true;
          break;
        }
      }

      if (!shouldCancel) return;
    }

    event.setCancelled(true);
    event.getPlayer().sendMessage(Message.get("mute.player.blocked").set("command", cmd).toString());
  }
}
