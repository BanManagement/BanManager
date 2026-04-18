package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ipaddr.IPAddressSeqRange;
import me.confuser.banmanager.common.ipaddr.IPAddressString;
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

    final Reason reason = parser.getReason();

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData actor = sender.getData();
      final IpRangeBanData ban = new IpRangeBanData(fromIp, toIp, actor, reason.getMessage(), isSilent);
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
        Message kickMessage = Message.get("baniprange.ip.kick")
            .set("id", ban.getId())
            .set("reason", ban.getReason())
            .set("actor", actor.getName());

        for (CommonPlayer onlinePlayer : getPlugin().getServer().getOnlinePlayers()) {
          if (ban.inRange(IPUtils.toIPAddress(onlinePlayer.getAddress()))) {
            onlinePlayer.kick(kickMessage.toString());
          }
        }
      });
    });

    return true;

  }
}

