package me.confuser.banmanager.common.commands;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.TimeLimitType;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class TempIpRangeBanCommand extends CommonCommand {

  public TempIpRangeBanCommand(BanManagerPlugin plugin) {
    super(plugin, "tempbaniprange", false, 2);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args.length < 3) {
      return false;
    }

    if (parser.isInvalidReason()) {
      Message.get("sender.error.invalidReason")
              .set("reason", parser.getReason().getMessage())
              .sendTo(sender);
      return true;
    }

    IPAddressString ip = new IPAddressString(parser.args[0]);

    if (!ip.isSequential()) {
      Message.get("baniprange.error.invalid").sendTo(sender);
      return true;
    }

    IPAddressSeqRange range = ip.getSequentialRange();

    final IPAddress fromIp = range.getLower();
    final IPAddress toIp = range.getUpper();

    if (getPlugin().getIpRangeBanStorage().isBanned(fromIp) || getPlugin().getIpRangeBanStorage().isBanned(toIp)) {
      Message.get("baniprange.error.exists").sendTo(sender);
      return true;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(parser.args[1], true);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    if (getPlugin().getConfig().getTimeLimits().isPastLimit(sender, TimeLimitType.IP_BAN, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;

    final String reason = parser.getReason().getMessage();

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData actor = sender.getData();
      final IpRangeBanData ban = new IpRangeBanData(fromIp, toIp, actor, reason, isSilent, expires);
      boolean created;

      try {
        created = getPlugin().getIpRangeBanStorage().ban(ban);
      } catch (SQLException e) {
        handlePunishmentCreateException(e, sender, Message.get("baniprange.error.exists"));
        return;
      }

      if (!created) {
        return;
      }

      // Find online players
      getPlugin().getScheduler().runSync(() -> {
        Message kickMessage = Message.get("tempbaniprange.ip.kick")
            .set("reason", ban.getReason())
            .set("actor", actor.getName())
            .set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));

        for (CommonPlayer onlinePlayer : getPlugin().getServer().getOnlinePlayers()) {
          if (ban.inRange(IPUtils.toIPAddress((onlinePlayer.getAddress())))) {
            onlinePlayer.kick(kickMessage.toString());
          }
        }
      });
    });

    return true;

  }

}
