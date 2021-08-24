package me.confuser.banmanager.common.commands;


import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class UnbanIpRangeCommand extends CommonCommand {

  public UnbanIpRangeCommand(BanManagerPlugin plugin) {
    super(plugin, "unbaniprange", false);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length < 1) {
      return false;
    }

    IPAddressString ip = new IPAddressString(parser.args[0]);
    final boolean isName;

    if (!ip.isSequential()) {
      isName = true;
    } else {
      isName = false;
    }

    IPAddressSeqRange range = ip.getSequentialRange();

    if (!isName && range == null) {
      Message.get("baniprange.error.invalid").sendTo(sender);
      return true;
    }

    final String reason = parser.args.length > 1 ? parser.getReason().getMessage() : "";

    getPlugin().getScheduler().runAsync(() -> {
      IPAddress fromIp = range.getLower();
      IPAddress toIp = range.getUpper();

      if (isName) {
        PlayerData player = getPlugin().getPlayerStorage().retrieve(parser.args[0], false);
        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", parser.args[0])
              .toString());
          return;
        }

        fromIp = player.getIp();
        toIp = player.getIp();
      }

      if (!getPlugin().getIpRangeBanStorage().isBanned(fromIp) && !getPlugin().getIpRangeBanStorage()
          .isBanned(toIp)) {
        Message message = Message.get("unbanip.error.noExists");
        message.set("ip", parser.args[0]);

        sender.sendMessage(message.toString());
        return;
      }

      IpRangeBanData ban = getPlugin().getIpRangeBanStorage().getBan(fromIp);

      if (ban == null) ban = getPlugin().getIpRangeBanStorage().getBan(toIp);

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
          .set("from", ban.getFromIp().toString())
          .set("to", ban.getToIp().toString())
          .set("actor", actor.getName())
          .set("id", ban.getId())
          .set("reason", reason);

      if (!sender.hasPermission("bm.notify.unbaniprange") || parser.isSilent()) {
        message.sendTo(sender);
      }

      if (!parser.isSilent()) {
        getPlugin().getServer().broadcast(message.toString(), "bm.notify.unbaniprange");
      }
    });

    return true;
  }
}
