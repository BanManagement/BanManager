package me.confuser.banmanager.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.IpMuteData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class TempIpMuteCommand extends AutoCompleteNameTabCommand<BanManager> {

  public TempIpMuteCommand() {
    super("tempmuteip");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    CommandParser parser = new CommandParser(args, 2);
    args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(command.getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    final boolean isSoft = parser.isSoft();

    if (isSoft && !sender.hasPermission(command.getPermission() + ".soft")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (args.length < 3) {
      return false;
    }

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, commandName, args);
      return true;
    }

    final String ipStr = args[0];
    final boolean isName = !InetAddresses.isInetAddress(ipStr);

    if (isName && ipStr.length() > 16) {
      Message message = Message.get("sender.error.invalidIp");
      message.set("ip", ipStr);

      sender.sendMessage(message.toString());
      return true;
    }

    if (isName) {
      Player onlinePlayer = plugin.getServer().getPlayer(ipStr);

      if (onlinePlayer != null && !sender.hasPermission("bm.exempt.override.muteip")
              && onlinePlayer.hasPermission("bm.exempt.muteip")) {
        Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
        return true;
      }
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(args[1], true);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    if (plugin.getConfiguration().getTimeLimits().isPastLimit(sender, TimeLimitType.IP_MUTE, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;
    final String reason = parser.getReason().getMessage();

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final Long ip = CommandUtils.getIp(ipStr);

        if (ip == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr).toString());
          return;
        }

        final boolean isMuted = plugin.getIpMuteStorage().isMuted(ip);

        if (isMuted && !sender.hasPermission("bm.command.tempmuteip.override")) {
          Message message = Message.get("muteip.error.exists");
          message.set("ip", ipStr);

          sender.sendMessage(message.toString());
          return;
        }

        final PlayerData actor = CommandUtils.getActor(sender);

        if (actor == null) return;
        if (isMuted) {
          IpMuteData mute = plugin.getIpMuteStorage().getMute(ip);

          if (mute != null) {
            try {
              plugin.getIpMuteStorage().unmute(mute, actor);
            } catch (SQLException e) {
              sender.sendMessage(Message.get("sender.error.exception").toString());
              e.printStackTrace();
              return;
            }
          }
        }

        final IpMuteData mute = new IpMuteData(ip, actor, reason, isSoft, expires);
        boolean created;

        try {
          created = plugin.getIpMuteStorage().mute(mute, isSilent);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!created) return;
        if (isSoft) return;

        // Find online players
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          public void run() {
            Message message = Message.get("tempmuteip.ip.disallowed")
                                     .set("reason", mute.getReason())
                                     .set("actor", actor.getName())
                                     .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));

            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
              if (IPUtils.toLong(onlinePlayer.getAddress().getAddress()) == ip) {
                message.sendTo(onlinePlayer);
              }
            }
          }
        });

      }

    });

    return true;
  }
}
