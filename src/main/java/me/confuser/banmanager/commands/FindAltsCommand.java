package me.confuser.banmanager.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class FindAltsCommand extends BukkitCommand<BanManager> {

  public FindAltsCommand() {
    super("alts");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 1) {
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

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final long ip;

        if (isName) {
          PlayerData srcPlayer = plugin.getPlayerStorage().retrieve(ipStr, false);
          if (srcPlayer == null) {
            sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr).toString());
            return;
          }

          ip = srcPlayer.getIp();
        } else {
          ip = IPUtils.toLong(ipStr);
        }

        List<PlayerData> players = plugin.getPlayerStorage().getDuplicates(ip);
        ArrayList<String> names = new ArrayList<>(players.size());

        for (PlayerData player : players) {
          names.add(player.getName());
        }

        sender.sendMessage(Message.get("alts.header").set("ip", ipStr).toString());

        if (names.isEmpty()) {
          sender.sendMessage(Message.get("none").toString());
          return;
        }

        sender.sendMessage(ChatColor.GOLD + StringUtils.join(names, ", "));
      }
    });

    return true;
  }
}
