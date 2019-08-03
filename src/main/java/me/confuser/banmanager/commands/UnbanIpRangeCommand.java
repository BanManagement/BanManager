package me.confuser.banmanager.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class UnbanIpRangeCommand extends BukkitCommand<BanManager> {

  public UnbanIpRangeCommand() {
    super("unbaniprange");
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
    long[] range = new long[2];
    final boolean isName;

    if (ipStr.contains("*")) {
      // Simple wildcard logic
      range = IPUtils.getRangeFromWildcard(ipStr);
      isName = false;
    } else if (ipStr.contains("/")) {
      // cidr notation
      range = IPUtils.getRangeFromCidrNotation(ipStr);
      isName = false;
    } else if (InetAddresses.isInetAddress(ipStr)) {
      range[0] = IPUtils.toLong(ipStr);
      range[1] = range[0];
      isName = false;
    } else if (ipStr.length() <= 16) {
      isName = true;
    } else {
      Message message = Message.get("sender.error.invalidIp");
      message.set("ip", ipStr);

      sender.sendMessage(message.toString());
      return true;
    }

    if (!isName && range == null) {
      Message.get("baniprange.error.invalid").sendTo(sender);
      return true;
    }

    final long[] ranges = range;
    final String reason = args.length > 1 ? CommandUtils.getReason(1, args).getMessage() : "";

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        long[] range = new long[2];

        if (isName) {
          PlayerData player = plugin.getPlayerStorage().retrieve(ipStr, false);
          if (player == null) {
            sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr)
                                      .toString());
            return;
          }

          range[0] = player.getIp();
          range[1] = player.getIp();
        } else {
          range = ranges;
        }

        if (!plugin.getIpRangeBanStorage().isBanned(range[0]) && !plugin.getIpRangeBanStorage().isBanned(range[1])) {
          Message message = Message.get("unbanip.error.noExists");
          message.set("ip", ipStr);

          sender.sendMessage(message.toString());
          return;
        }

        IpRangeBanData ban = plugin.getIpRangeBanStorage().getBan(range[0]);

        if (ban == null) ban = plugin.getIpRangeBanStorage().getBan(range[1]);

        PlayerData actor;

        if (sender instanceof Player) {
          try {
            actor = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes((Player) sender));
          } catch (SQLException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        boolean unbanned;

        try {
          unbanned = plugin.getIpRangeBanStorage().unban(ban, actor, reason);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!unbanned) {
          return;
        }

        Message message = Message.get("unbaniprange.notify");
        message
                .set("from", IPUtils.toString(ban.getFromIp()))
                .set("to", IPUtils.toString(ban.getToIp()))
                .set("actor", actor.getName())
                .set("reason", reason);

        if (!sender.hasPermission("bm.notify.unbaniprange")) {
          message.sendTo(sender);
        }

        CommandUtils.broadcast(message.toString(), "bm.notify.unbaniprange");
      }

    });

    return true;
  }
}
