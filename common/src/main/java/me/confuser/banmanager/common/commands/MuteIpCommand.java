package me.confuser.banmanager.common.commands;

import com.google.common.net.InetAddresses;
import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class MuteIpCommand extends CommonCommand {

  public MuteIpCommand(BanManagerPlugin plugin) {
    super(plugin, "muteip", false, 1);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    final boolean isSoft = parser.isSoft();

    if (isSoft && !sender.hasPermission(getPermission() + ".soft")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args.length < 2) {
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

    if (isName) {
      CommonPlayer onlinePlayer = getPlugin().getServer().getPlayer(ipStr);

      if (onlinePlayer != null && !sender.hasPermission("bm.exempt.override.muteip")
          && onlinePlayer.hasPermission("bm.exempt.muteip")) {
        Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
        return true;
      }
    }

    final String reason = parser.getReason().getMessage();

    getPlugin().getScheduler().runAsync(() -> {
      final IPAddress ip = getIp(ipStr);

      if (ip == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr).toString());
        return;
      }

      final boolean isMuted = getPlugin().getIpMuteStorage().isMuted(ip);

      if (isMuted && !sender.hasPermission("bm.command.muteip.override")) {
        Message message = Message.get("muteip.error.exists");
        message.set("ip", ipStr);

        sender.sendMessage(message.toString());
        return;
      }

      final PlayerData actor = sender.getData();

      if (actor == null) return;

      if (isMuted) {
        IpMuteData mute = getPlugin().getIpMuteStorage().getMute(ip);

        if (mute != null) {
          try {
            getPlugin().getIpMuteStorage().unmute(mute, actor);
          } catch (SQLException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }
        }
      }

      final IpMuteData mute = new IpMuteData(ip, actor, reason, isSoft);
      boolean created;

      try {
        created = getPlugin().getIpMuteStorage().mute(mute, isSilent);
      } catch (SQLException e) {
        handlePunishmentCreateException(e, sender, Message.get("muteip.error.exists").set("ip",
            ipStr));
        return;
      }

      if (!created) return;
      if (isSoft) return;

      // Find online players
      getPlugin().getScheduler().runSync(() -> {
        Message message = Message.get("muteip.ip.disallowed")
            .set("reason", mute.getReason())
            .set("actor", actor.getName());

        for (CommonPlayer onlinePlayer : getPlugin().getServer().getOnlinePlayers()) {
          if (IPUtils.toIPAddress(onlinePlayer.getAddress()).equals(ip)) {
            onlinePlayer.sendMessage(message);
          }
        }
      });
    });

    return true;
  }

}
