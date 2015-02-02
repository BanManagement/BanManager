package me.confuser.banmanager.commands.external;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.external.ExternalIpBanRecordData;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class UnbanIpAllCommand extends BukkitCommand<BanManager> {

  public UnbanIpAllCommand() {
    super("unbanipall");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 1) {
      return false;
    }

    // Check if UUID vs name
    final String playerName = args[0];
    final String ipStr = args[0];
    final boolean isName = !InetAddresses.isInetAddress(ipStr);

    if (isName && ipStr.length() > 16) {
      Message message = Message.get("invalidIp");
      message.set("ip", ipStr);

      sender.sendMessage(message.toString());
      return true;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final long ip;

        if (isName) {
          PlayerData player = plugin.getPlayerStorage().retrieve(ipStr, false);
          if (player == null) {
            sender.sendMessage(Message.get("playerNotFound").set("player", ipStr).toString());
            return;
          }

          ip = player.getIp();
        } else {
          ip = IPUtils.toLong(ipStr);
        }

        IpBanData ban = plugin.getIpBanStorage().getBan(ip);

        if (ban == null) {
          Message message = Message.get("unbanip.error.noexists");
          message.set("ip", ipStr);

          sender.sendMessage(message.toString());
          return;
        }

        PlayerData actor;

        if (sender instanceof Player) {
          actor = plugin.getPlayerStorage().getOnline((Player) sender);
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        ExternalIpBanRecordData record = new ExternalIpBanRecordData(ban.getIp(), actor);

        int unbanned;

        try {
          unbanned = plugin.getExternalIpBanRecordStorage().create(record);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("errorOccurred").toString());
          e.printStackTrace();
          return;
        }

        if (unbanned == 0) {
          return;
        }

        Message.get("unbanipall.player.notify")
               .set("actor", actor.getName())
               .set("ip", ban.getIp())
               .sendTo(sender);
      }

    });

    return true;
  }
}
