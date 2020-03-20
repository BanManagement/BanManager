package me.confuser.banmanager.common.commands;


import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.TimeLimitType;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class TempIpMuteCommand extends CommonCommand {

  public TempIpMuteCommand(BanManagerPlugin plugin) {
    super(plugin, "tempmuteip", false, 2);
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

    if (parser.args.length < 3) {
      return false;
    }

    if (parser.isInvalidReason()) {
      Message.get("sender.error.invalidReason")
              .set("reason", parser.getReason().getMessage())
              .sendTo(sender);
      return true;
    }

    if (parser.args[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    final String ipStr = parser.args[0];
    final boolean isName = !IPUtils.isValid(ipStr);

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

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(parser.args[1], true);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    if (getPlugin().getConfig().getTimeLimits().isPastLimit(sender, TimeLimitType.IP_MUTE, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;
    final String reason = parser.getReason().getMessage();

    getPlugin().getScheduler().runAsync(() -> {
      final IPAddress ip = getIp(ipStr);

      if (ip == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr).toString());
        return;
      }

      final boolean isMuted = getPlugin().getIpMuteStorage().isMuted(ip);

      if (isMuted && !sender.hasPermission("bm.command.tempmuteip.override")) {
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

      final IpMuteData mute = new IpMuteData(ip, actor, reason, isSilent, isSoft, expires);
      boolean created;

      try {
        created = getPlugin().getIpMuteStorage().mute(mute);
      } catch (SQLException e) {
        handlePunishmentCreateException(e, sender, Message.get("muteip.error.exists").set("ip",
            ipStr));
        return;
      }

      if (!created) return;
      if (isSoft) return;

      // Find online players
      getPlugin().getScheduler().runSync(() -> {
        Message message = Message.get("tempmuteip.ip.disallowed")
            .set("reason", mute.getReason())
            .set("actor", actor.getName())
            .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));

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
