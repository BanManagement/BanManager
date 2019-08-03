package me.confuser.banmanager.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class UnmuteIpCommand extends BukkitCommand<BanManager> {

  public UnmuteIpCommand() {
    super("unmuteip");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 1) {
      return false;
    }

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, commandName, args);
      return true;
    }

    final String ipStr = args[0];
    final boolean isName = !InetAddresses.isInetAddress(ipStr);

    if (isName && ipStr.length() > 16) {
      Message message = Message.get("sender.error.invalidIp");
      message.set("ip", ipStr);

      sender.sendMessage(message.toString());
      return true;
    }

    final String reason = args.length > 1 ? CommandUtils.getReason(1, args).getMessage() : "";

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final Long ip = CommandUtils.getIp(ipStr);

        if (ip == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr).toString());
          return;
        }

        if (!plugin.getIpMuteStorage().isMuted(ip)) {
          Message message = Message.get("unmuteip.error.noExists");
          message.set("ip", ipStr);

          sender.sendMessage(message.toString());
          return;
        }

        IpMuteData mute = plugin.getIpMuteStorage().getMute(ip);

        final PlayerData actor = CommandUtils.getActor(sender);

        if (actor == null) return;

        boolean unmuted;

        try {
          unmuted = plugin.getIpMuteStorage().unmute(mute, actor, reason);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!unmuted) {
          return;
        }

        Message message = Message.get("unmuteip.notify");
        message
                .set("ip", ipStr)
                .set("actor", actor.getName())
                .set("reason", reason);

        if (!sender.hasPermission("bm.notify.unmuteip")) {
          message.sendTo(sender);
        }

        CommandUtils.broadcast(message.toString(), "bm.notify.unmuteip");
      }

    });

    return true;
  }
}
