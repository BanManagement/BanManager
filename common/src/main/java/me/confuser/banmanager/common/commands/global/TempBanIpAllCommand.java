package me.confuser.banmanager.common.commands.global;


import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.configs.TimeLimitType;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.global.GlobalIpBanData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class TempBanIpAllCommand extends CommonCommand {

  public TempBanIpAllCommand(BanManagerPlugin plugin) {
    super(plugin, "tempbanipall", true, 2);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.getArgs().length < 3) {
      return false;
    }

    final String ipStr = parser.getArgs()[0];
    final boolean isName = IPUtils.isValid(ipStr);

    if (isName && ipStr.length() > 16) {
      Message message = Message.get("sender.error.invalidIp");
      message.set("ip", ipStr);

      sender.sendMessage(message.toString());
      return true;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(parser.getArgs()[1], true);
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
      final IPAddress ip = getIp(ipStr);

      final PlayerData actor = sender.getData();
      final GlobalIpBanData ban = new GlobalIpBanData(ip, actor, reason, expires);
      int created;

      try {
        created = getPlugin().getGlobalIpBanStorage().create(ban);
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
        return;
      }

      if (created != 1) {
        return;
      }

      Message.get("tempbanipall.notify")
          .set("ip", ipStr)
          .set("actor", ban.getActorName())
          .set("reason", ban.getReason())
          .set("expires", DateUtils.getDifferenceFormat(ban.getExpires()))
          .sendTo(sender);
    });

    return true;
  }

}
