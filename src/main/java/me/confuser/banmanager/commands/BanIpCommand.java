package me.confuser.banmanager.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.banmanager.util.parsers.Reason;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class BanIpCommand extends AutoCompleteNameTabCommand<BanManager> {

  public BanIpCommand() {
    super("banip");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    CommandParser parser = new CommandParser(args);
    args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(command.getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (args.length < 2) {
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

      if (onlinePlayer != null && !sender.hasPermission("bm.exempt.override.banip")
              && onlinePlayer.hasPermission("bm.exempt.banip")) {
        Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
        return true;
      }
    }

    final Reason reason = CommandUtils.getReason(1, args);

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final Long ip = CommandUtils.getIp(ipStr);

        if (ip == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr).toString());
          return;
        }

        final boolean isBanned = plugin.getIpBanStorage().isBanned(ip);

        if (isBanned && !sender.hasPermission("bm.command.banip.override")) {
          Message message = Message.get("banip.error.exists");
          message.set("ip", ipStr);

          sender.sendMessage(message.toString());
          return;
        }

        final PlayerData actor = CommandUtils.getActor(sender);

        if (actor == null) return;

        if (isBanned) {
          IpBanData ban = plugin.getIpBanStorage().getBan(ip);

          if (ban != null) {
            try {
              plugin.getIpBanStorage().unban(ban, actor);
            } catch (SQLException e) {
              sender.sendMessage(Message.get("sender.error.exception").toString());
              e.printStackTrace();
              return;
            }
          }
        }

        final IpBanData ban = new IpBanData(ip, actor, reason.getMessage());
        boolean created;

        try {
          created = plugin.getIpBanStorage().ban(ban, isSilent);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!created) {
          return;
        }

        // Find online players
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          public void run() {
            Message kickMessage = Message.get("banip.ip.kick")
                                         .set("reason", ban.getReason())
                                         .set("actor", actor.getName());

            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
              if (IPUtils.toLong(onlinePlayer.getAddress().getAddress()) == ip) {
                onlinePlayer.kickPlayer(kickMessage.toString());
              }
            }
          }
        });
      }

    });

    return true;
  }

}
