package me.confuser.banmanager.common.commands.global;

import com.google.common.net.InetAddresses;
import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.global.GlobalIpBanRecordData;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class UnbanIpAllCommand extends CommonCommand {

  public UnbanIpAllCommand(BanManagerPlugin plugin) {
    super(plugin, "unbanipall", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.getArgs().length < 1) {
      return false;
    }

    // Check if UUID vs name
    final String ipStr = parser.getArgs()[0];
    final boolean isName = !InetAddresses.isInetAddress(ipStr);

    if (isName && ipStr.length() > 16) {
      Message message = Message.get("invalidIp");
      message.set("ip", ipStr);

      sender.sendMessage(message.toString());
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      final IPAddress ip = getIp(ipStr);

      IpBanData ban = getPlugin().getIpBanStorage().getBan(ip);

      if (ban == null) {
        Message message = Message.get("unbanip.error.noExists");
        message.set("ip", ipStr);

        sender.sendMessage(message.toString());
        return;
      }

      PlayerData actor = sender.getData();
      GlobalIpBanRecordData record = new GlobalIpBanRecordData(ban.getIp(), actor);

      int unbanned;

      try {
        unbanned = getPlugin().getGlobalIpBanRecordStorage().create(record);
      } catch (SQLException e) {
        sender.sendMessage(Message.get("errorOccurred").toString());
        e.printStackTrace();
        return;
      }

      if (unbanned == 0) {
        return;
      }

      Message.get("unbanipall.notify")
          .set("actor", actor.getName())
          .set("ip", ban.getIp().toString())
          .sendTo(sender);
    });

    return true;
  }
}
