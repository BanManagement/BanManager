package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.util.BukkitUUIDUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;

public class BukkitCommandListener implements Listener {

  private final BMBukkitPlugin plugin;

  public BukkitCommandListener(BMBukkitPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent event) {
    UUID id = BukkitUUIDUtils.getUUID(event.getPlayer());

    if (!plugin.getPlayerMuteStorage().isMuted(id)) {
      return;
    }

    // Split the command
    String[] args = event.getMessage().split(" ", 6);

    // Get rid of the first /
    String cmd = args[0].replace("/", "").toLowerCase();

    boolean isSoft = plugin.getPlayerMuteStorage().getMute(id).isSoft();
    boolean deepCheck = isSoft ? !plugin.getConfiguration().isSoftBlockedCommand(cmd) : !plugin.getConfiguration().isBlockedCommand(cmd);

    if (deepCheck) {
      // Check if arguments blocked
      boolean shouldCancel = false;
      for (int i = 1; i < args.length; i++) {
        String check = cmd + " " + StringUtils.join(args, " ", 1, i + 1);

        if ((isSoft && plugin.getConfiguration().isSoftBlockedCommand(check)) || plugin.getConfiguration().isBlockedCommand(check)) {
          shouldCancel = true;
          cmd = check;
          break;
        }
      }

      if (!shouldCancel) return;
    }

    event.setCancelled(true);

    if (!isSoft) {
      Message.MUTE_PLAYER_BLOCKED.send((Sender) event.getPlayer(), "command", cmd);
    }
  }
}
