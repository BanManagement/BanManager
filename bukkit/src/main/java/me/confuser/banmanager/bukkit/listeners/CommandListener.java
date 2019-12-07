package me.confuser.banmanager.bukkit.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.util.Message;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

  private BanManagerPlugin plugin;

  public CommandListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent event) {
    if (!plugin.getPlayerMuteStorage().isMuted(event.getPlayer().getUniqueId())) {
      return;
    }

    // Split the command
    String[] args = event.getMessage().split(" ", 6);

    // Get rid of the first /
    String cmd = args[0].replace("/", "").toLowerCase();

    boolean isSoft = plugin.getPlayerMuteStorage().getMute(event.getPlayer().getUniqueId()).isSoft();
    boolean deepCheck = isSoft ? !plugin.getConfig().isSoftBlockedCommand(cmd) : !plugin.getConfig()
                                                                                        .isBlockedCommand(cmd);

    if (deepCheck) {
      // Check if arguments blocked
      boolean shouldCancel = false;
      for (int i = 1; i < args.length; i++) {
        String check = cmd + " " + StringUtils.join(args, " ", 1, i + 1);

        if ((isSoft && plugin.getConfig().isSoftBlockedCommand(check)) || plugin.getConfig().isBlockedCommand(check)) {
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
