package me.confuser.banmanager.sponge.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.util.Message;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;

public class CommandListener {

  private BanManagerPlugin plugin;

  public CommandListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @Listener(order = Order.FIRST, beforeModifications = true)
  public void onCommand(SendCommandEvent event, @First Player player) {
    if (!plugin.getPlayerMuteStorage().isMuted(player.getUniqueId())) {
      return;
    }

    // Split the command
    String[] args = event.getArguments().split(" ", 6);

    // Get rid of the first /
    String cmd = event.getCommand().toLowerCase();

    boolean isSoft = plugin.getPlayerMuteStorage().getMute(player.getUniqueId()).isSoft();
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

    if (!isSoft) player.sendMessage(Text.of(Message.get("mute.player.blocked").set("command", cmd).toString()));
  }
}
