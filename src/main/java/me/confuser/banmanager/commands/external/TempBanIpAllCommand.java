package me.confuser.banmanager.commands.external;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.external.ExternalIpBanData;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class TempBanIpAllCommand extends BukkitCommand<BanManager> {

  public TempBanIpAllCommand() {
    super("tempbanipall");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 3) {
      return false;
    }

    final String ipStr = args[0];
    final boolean isName = !InetAddresses.isInetAddress(ipStr);

    if (isName && ipStr.length() > 16) {
      Message message = Message.get("sender.error.invalidIp");
      message.set("ip", ipStr);

      sender.sendMessage(message.toString());
      return true;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(args[1], true);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    if (plugin.getConfiguration().getTimeLimits().isPastLimit(sender, TimeLimitType.IP_BAN, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;

    final String reason = StringUtils.join(args, " ", 2, args.length);

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final long ip;

        if (isName) {
          PlayerData player = plugin.getPlayerStorage().retrieve(ipStr, false);
          if (player == null) {
            sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr).toString());
            return;
          }

          ip = player.getIp();
        } else {
          ip = IPUtils.toLong(ipStr);
        }

        final PlayerData actor;

        if (sender instanceof Player) {
          actor = plugin.getPlayerStorage().getOnline((Player) sender);
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        final ExternalIpBanData ban = new ExternalIpBanData(ip, actor, reason, expires);
        int created;

        try {
          created = plugin.getExternalIpBanStorage().create(ban);
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
      }

    });

    return true;
  }

}
