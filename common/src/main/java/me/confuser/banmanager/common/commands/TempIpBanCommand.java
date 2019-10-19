package me.confuser.banmanager.common.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.TimeLimitType;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class TempIpBanCommand extends CommonCommand {

  public TempIpBanCommand(BanManagerPlugin plugin) {
    super(plugin, "tempbanip", false, 2);
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

      if (onlinePlayer != null && !sender.hasPermission("bm.exempt.override.banip")
          && onlinePlayer.hasPermission("bm.exempt.banip")) {
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

    if (getPlugin().getConfig().getTimeLimits().isPastLimit(sender, TimeLimitType.IP_BAN, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;
    final String reason = parser.getReason().getMessage();

    getPlugin().getScheduler().runAsync(new Runnable() {

      @Override
      public void run() {
        final Long ip = getIp(ipStr);

        if (ip == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr).toString());
          return;
        }

        final boolean isBanned = getPlugin().getIpBanStorage().isBanned(ip);

        if (isBanned && !sender.hasPermission("bm.command.tempbanip.override")) {
          Message message = Message.get("banip.error.exists");
          message.set("ip", ipStr);

          sender.sendMessage(message.toString());
          return;
        }

        final PlayerData actor = sender.getData();

        if (actor == null) return;

        if (isBanned) {
          IpBanData ban = getPlugin().getIpBanStorage().getBan(ip);

          if (ban != null) {
            try {
              getPlugin().getIpBanStorage().unban(ban, actor);
            } catch (SQLException e) {
              sender.sendMessage(Message.get("sender.error.exception").toString());
              e.printStackTrace();
              return;
            }
          }
        }

        final IpBanData ban = new IpBanData(ip, actor, reason, expires);
        boolean created;

        try {
          created = getPlugin().getIpBanStorage().ban(ban, isSilent);
        } catch (SQLException e) {
          handlePunishmentCreateException(e, sender, Message.get("banip.error.exists").set("ip",
              ipStr));
          return;
        }

        if (!created) {
          return;
        }

        // Find online players
        getPlugin().getScheduler().runAsync(() -> {
          Message kickMessage = Message.get("tempbanip.ip.kick")
              .set("reason", ban.getReason())
              .set("actor", actor.getName())
              .set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));;

          for (CommonPlayer onlinePlayer : getPlugin().getServer().getOnlinePlayers()) {
            if (IPUtils.toLong(onlinePlayer.getAddress()) == ip) {
              onlinePlayer.kick(kickMessage.toString());
            }
          }
        });

      }

    });

    return true;
  }
}
