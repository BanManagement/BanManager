package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpRangeBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class BanIpRangeCommand extends BukkitCommand<BanManager> {

  public BanIpRangeCommand() {
    super("baniprange");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 2) {
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

    final String reason = StringUtils.join(args, " ", 1, args.length);

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData actor;

        if (sender instanceof Player) {
          actor = plugin.getPlayerStorage().getOnline((Player) sender);
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        final IpRangeBanData ban = new IpRangeBanData(fromIp, toIp, actor, reason);
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
            Message kickMessage = Message.get("baniprange.ip.kick")
                                         .set("reason", ban.getReason())
                                         .set("actor", actor.getName());

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
