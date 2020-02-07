package me.confuser.banmanager.common.commands;

import com.google.common.net.InetAddresses;
import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class UnmuteIpCommand extends CommonCommand {

  public UnmuteIpCommand(BanManagerPlugin plugin) {
    super(plugin, "unmuteip", false);
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

    final String reason = parser.args.length > 1 ? parser.getReason(1).getMessage() : "";

    getPlugin().getScheduler().runAsync(() -> {
      final IPAddress ip = getIp(ipStr);

      if (ip == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr).toString());
        return;
      }

      if (!getPlugin().getIpMuteStorage().isMuted(ip)) {
        Message message = Message.get("unmuteip.error.noExists");
        message.set("ip", ipStr);

        sender.sendMessage(message.toString());
        return;
      }

      IpMuteData mute = getPlugin().getIpMuteStorage().getMute(ip);

      final PlayerData actor = sender.getData();

      if (actor == null) return;

      boolean unmuted;

      try {
        unmuted = getPlugin().getIpMuteStorage().unmute(mute, actor, reason);
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
        return;
      }

      if (!unmuted) {
        return;
      }

      Message message = Message.get("unmuteip.notify");
      message
          .set("ip", ipStr)
          .set("actor", actor.getName())
          .set("reason", reason);

      if (!sender.hasPermission("bm.notify.unmuteip")) {
        message.sendTo(sender);
      }

      getPlugin().getServer().broadcast(message.toString(), "bm.notify.unmuteip");
    });

    return true;
  }
}
