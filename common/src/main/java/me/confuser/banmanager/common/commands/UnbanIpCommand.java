package me.confuser.banmanager.common.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class UnbanIpCommand extends CommonCommand {

  public UnbanIpCommand(BanManagerPlugin plugin) {
    super(plugin, "unbanip", false);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length < 1) {
      return false;
    }

    final String ipStr = parser.args[0];
    final boolean isName = !InetAddresses.isInetAddress(ipStr);

    if (isName && ipStr.length() > 16) {
      Message message = Message.get("sender.error.invalidIp");
      message.set("ip", ipStr);

      sender.sendMessage(message.toString());
      return true;
    }

    final String reason = parser.args.length > 1 ? parser.getReason().getMessage() : "";

    getPlugin().getScheduler().runAsync(() -> {
      final long ip;

      if (isName) {
        PlayerData player = getPlugin().getPlayerStorage().retrieve(ipStr, false);
        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr)
                                    .toString());
          return;
        }

        ip = player.getIp();
      } else {
        ip = IPUtils.toLong(ipStr);
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
        unbanned = getPlugin().getIpBanStorage().unban(ban, actor, reason);
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
              .set("reason", reason);

      if (!sender.hasPermission("bm.notify.unbanip")) {
        message.sendTo(sender);
      }

      getPlugin().getServer().broadcast(message.toString(), "bm.notify.unbanip");
    });

    return true;
  }
}
