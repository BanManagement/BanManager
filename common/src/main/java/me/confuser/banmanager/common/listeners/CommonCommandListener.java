package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.StringUtils;

public class CommonCommandListener {
  private BanManagerPlugin plugin;

  public CommonCommandListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public boolean onCommand(CommonPlayer player, String cmd, String[] args) {
    if (!plugin.getPlayerMuteStorage().isMuted(player.getUniqueId())) {
      return false;
    }

    int startIndex = 0;

    if (args[0].replace("/", "").toLowerCase().equals(cmd)) {
      startIndex = 1;
    }

    boolean isSoft = plugin.getPlayerMuteStorage().getMute(player.getUniqueId()).isSoft();
    boolean deepCheck = isSoft ? !plugin.getConfig().isSoftBlockedCommand(cmd) : !plugin.getConfig().isBlockedCommand(cmd);

    if (deepCheck) {
      // Check if arguments blocked
      boolean shouldCancel = false;
      for (int i = startIndex; i < args.length; i++) {
        String check = cmd + " " + StringUtils.join(args, " ", startIndex, i + 1);

        if ((isSoft && plugin.getConfig().isSoftBlockedCommand(check)) || plugin.getConfig().isBlockedCommand(check)) {
          shouldCancel = true;
          cmd = check;
          break;
        }
      }

      if (!shouldCancel) return false;
    }

    if (!isSoft) player.sendMessage(Message.get("mute.player.blocked").set("command", cmd).toString());

    return true;
  }
}
