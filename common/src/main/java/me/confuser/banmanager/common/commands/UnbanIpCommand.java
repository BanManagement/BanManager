package me.confuser.banmanager.common.commands;


import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.parsers.UnbanCommandParser;

import java.sql.SQLException;

public class UnbanIpCommand extends CommonCommand {

  public UnbanIpCommand(BanManagerPlugin plugin) {
    super(plugin, "unbanip", false, UnbanCommandParser.class, 0);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser originalParser) {
    final UnbanCommandParser parser = (UnbanCommandParser) originalParser;
    final boolean isDelete = parser.isDelete();

    if (isDelete && !sender.hasPermission(getPermission() + ".delete")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args.length < 1) {
      return false;
    }

    final String ipStr = parser.args[0];
    final boolean isName = !IPUtils.isValid(ipStr);

    if (isName && ipStr.length() > 16) {
      Message message = Message.get("sender.error.invalidIp");
      message.set("ip", ipStr);

      sender.sendMessage(message.toString());
      return true;
    }

    final String reason = parser.args.length > 1 ? parser.getReason().getMessage() : "";

    getPlugin().getScheduler().runAsync(() -> {
      final IPAddress ip;

      if (isName) {
        PlayerData player = getPlugin().getPlayerStorage().retrieve(ipStr, false);
        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr)
                                    .toString());
          return;
        }

        ip = player.getIp();
      } else {
        try {
          ip = new IPAddressString(ipStr).toAddress();
        } catch (AddressStringException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }
      }

      if (!getPlugin().getIpBanStorage().isBanned(ip)) {
        Message message = Message.get("unbanip.error.noExists");
        message.set("ip", ipStr);

        sender.sendMessage(message.toString());
        return;
      }

      IpBanData ban = getPlugin().getIpBanStorage().getBan(ip);
      PlayerData actor = sender.getData();

      boolean unbanned;

      try {
        unbanned = getPlugin().getIpBanStorage().unban(ban, actor, reason, isDelete);
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
        return;
      }

      if (!unbanned) {
        return;
      }

      Message message = Message.get("unbanip.notify");
      message
              .set("ip", ipStr)
              .set("actor", actor.getName())
              .set("id", ban.getId())
              .set("reason", reason);

      if (!sender.hasPermission("bm.notify.unbanip") || parser.isSilent()) {
        message.sendTo(sender);
      }

      if (!parser.isSilent()) {
        getPlugin().getServer().broadcast(message.toString(), "bm.notify.unbanip");
      }
    });

    return true;
  }
}
