package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class BanIpRangeCommand extends CommonCommand {

  public BanIpRangeCommand(BanManagerPlugin plugin) {
    super(plugin, "baniprange", false, 1);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args.length < 2) {
      return false;
    }

    String ipStr = parser.args[0];
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

    if (getPlugin().getIpRangeBanStorage().isBanned(fromIp) || getPlugin().getIpRangeBanStorage().isBanned(toIp)) {
      Message.get("baniprange.error.exists").sendTo(sender);
      return true;
    }

    final Reason reason = parser.getReason();

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData actor = sender.getData();
      final IpRangeBanData ban = new IpRangeBanData(fromIp, toIp, actor, reason.getMessage());
      boolean created;

      try {
        created = getPlugin().getIpRangeBanStorage().ban(ban, isSilent);
      } catch (SQLException e) {
        handlePunishmentCreateException(e, sender, Message.get("baniprange.error.exists"));
        return;
      }

      if (!created) {
        return;
      }

      // Find online players
      getPlugin().getScheduler().runSync(() -> {
        Message kickMessage = Message.get("baniprange.ip.kick")
            .set("reason", ban.getReason())
            .set("actor", actor.getName());

        for (CommonPlayer onlinePlayer : getPlugin().getServer().getOnlinePlayers()) {
          if (ban.inRange(IPUtils.toLong(onlinePlayer.getAddress()))) {
            onlinePlayer.kick(kickMessage.toString());
          }
        }
      });
    });

    return true;

  }
}

