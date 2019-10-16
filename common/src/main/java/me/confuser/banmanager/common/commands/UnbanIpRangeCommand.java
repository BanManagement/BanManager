package me.confuser.banmanager.common.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class UnbanIpRangeCommand extends CommonCommand {

  public UnbanIpRangeCommand(BanManagerPlugin plugin) {
    super(plugin, "unbaniprange");
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length < 1) {
      return false;
    }

    final String ipStr = parser.args[0];
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
    final String reason = parser.args.length > 1 ? parser.getReason().getMessage() : "";

    getPlugin().getScheduler().runAsync(() -> {
      long[] range1 = new long[2];

      if (isName) {
        PlayerData player = getPlugin().getPlayerStorage().retrieve(ipStr, false);
        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr)
                                    .toString());
          return;
        }

        range1[0] = player.getIp();
        range1[1] = player.getIp();
      } else {
        range1 = ranges;
      }

      if (!getPlugin().getIpRangeBanStorage().isBanned(range1[0]) && !getPlugin().getIpRangeBanStorage()
                                                                                 .isBanned(range1[1])) {
        Message message = Message.get("unbanip.error.noExists");
        message.set("ip", ipStr);

        sender.sendMessage(message.toString());
        return;
      }

      IpRangeBanData ban = getPlugin().getIpRangeBanStorage().getBan(range1[0]);

      if (ban == null) ban = getPlugin().getIpRangeBanStorage().getBan(range1[1]);

      PlayerData actor = sender.getData();

      boolean unbanned;

      try {
        unbanned = getPlugin().getIpRangeBanStorage().unban(ban, actor, reason);
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

      getPlugin().getServer().broadcast(message.toString(), "bm.notify.unbaniprange");
    });

    return true;
  }
}
