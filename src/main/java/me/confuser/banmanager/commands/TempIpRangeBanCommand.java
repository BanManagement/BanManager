package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.IpRangeBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class TempIpRangeBanCommand extends BukkitCommand<BanManager> {

  public TempIpRangeBanCommand() {
    super("tempbaniprange");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 3) {
      return false;
    }

    String ipStr = args[0];
    long[] range = null;

    if (ipStr.contains("*")) {
      // Simple wildcard logic
      range = IPUtils.getRangeFromWildcard(ipStr);
    } else if (ipStr.contains("/")) {
      // cidr notation
      range = IPUtils.getRangeFromCidrNotation(ipStr);
    }

    if (range == null) {
      Message.get("baniprange.error.invalid").sendTo(sender);
      return true;
    }

    final long fromIp = range[0];
    final long toIp = range[1];

    if (fromIp > toIp) {
      Message.get("baniprange.error.minMax").sendTo(sender);
      return true;
    }

    if (plugin.getIpRangeBanStorage().isBanned(fromIp) || plugin.getIpRangeBanStorage().isBanned(toIp)) {
      Message.get("baniprange.error.exists").sendTo(sender);
      return true;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(args[1], true);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    if (plugin.getConfiguration().getTimeLimits().isPastLimit(sender, TimeLimitType.IP_BAN, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;

    final String reason = StringUtils.join(args, " ", 2, args.length);

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData actor;

        if (sender instanceof Player) {
          actor = plugin.getPlayerStorage().getOnline((Player) sender);
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        final IpRangeBanData ban = new IpRangeBanData(fromIp, toIp, actor, reason, expires);
        boolean created = false;

        try {
          created = plugin.getIpRangeBanStorage().ban(ban);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!created) {
          return;
        }

        // Find online players
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          public void run() {
            Message kickMessage = Message.get("tempbaniprange.ip.kick")
                                         .set("reason", ban.getReason())
                                         .set("actor", actor.getName())
                                         .set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));

            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
              if (ban.inRange(IPUtils.toLong(onlinePlayer.getAddress().getAddress()))) {
                onlinePlayer.kickPlayer(kickMessage.toString());
              }
            }
          }
        });
      }
    });

    return true;

  }

}
