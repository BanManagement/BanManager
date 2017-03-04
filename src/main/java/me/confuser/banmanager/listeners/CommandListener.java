package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;
import org.apache.commons.lang.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;

public class CommandListener extends Listeners<BanManager> {

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent event) {
    UUID id = UUIDUtils.getUUID(event.getPlayer());

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

    if (!isSoft) event.getPlayer().sendMessage(Message.get("mute.player.blocked").set("command", cmd).toString());
  }
}
