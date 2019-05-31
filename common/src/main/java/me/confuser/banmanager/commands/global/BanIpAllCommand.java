package me.confuser.banmanager.commands.global;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.global.GlobalIpBanData;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class BanIpAllCommand extends BukkitCommand<BanManager> {

  public BanIpAllCommand() {
    super("banipall");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 2) {
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

    final String reason = StringUtils.join(args, " ", 1, args.length);

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
          try {
            actor = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes((Player) sender));
          } catch (SQLException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        final GlobalIpBanData ban = new GlobalIpBanData(ip, actor, reason);
        int created;

        try {
          created = plugin.getGlobalIpBanStorage().create(ban);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (created != 1) {
          return;
        }

        Message.get("banipall.notify")
               .set("actor", ban.getActorName())
               .set("reason", ban.getReason())
               .set("ip", ipStr)
               .sendTo(sender);
      }

    });

    return true;
  }

}
