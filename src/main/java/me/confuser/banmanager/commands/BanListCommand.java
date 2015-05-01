package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.IpRangeBanData;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BanListCommand extends BukkitCommand<BanManager> {

  public BanListCommand() {
    super("banlist");
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length > 1) return false;

    String type = "players";

    if (args.length == 1) {
      type = args[0];
    }

    StringBuilder list = new StringBuilder();
    int total = 0;

    if (type.startsWith("play")) {
      if (!sender.hasPermission(command.getPermission() + ".players")) {
        sender.sendMessage(Message.getString("sender.error.noPermission"));
        return true;
      }

      for (PlayerBanData ban : plugin.getPlayerBanStorage().getBans().values()) {
        list.append(ban.getPlayer().getName());
        list.append(", ");

        total++;
      }
    } else if (type.startsWith("ipr")) {
      if (!sender.hasPermission(command.getPermission() + ".ipranges")) {
        sender.sendMessage(Message.getString("sender.error.noPermission"));
        return true;
      }

      for (IpRangeBanData ban : plugin.getIpRangeBanStorage().getBans().values()) {
        list.append(IPUtils.toString(ban.getFromIp()));
        list.append(" - ");
        list.append(IPUtils.toString(ban.getToIp()));
        list.append(", ");

        total++;
      }
    } else if (type.startsWith("ip")) {
      if (!sender.hasPermission(command.getPermission() + ".ips")) {
        sender.sendMessage(Message.getString("sender.error.noPermission"));
        return true;
      }

      for (IpBanData ban : plugin.getIpBanStorage().getBans().values()) {
        list.append(IPUtils.toString(ban.getIp()));
        list.append(", ");

        total++;
      }
    } else {
      return false;
    }

    if (list.length() >= 2) list.setLength(list.length() - 2);

    Message.get("banlist.header").set("bans", total).set("type", type).sendTo(sender);
    if (list.length() > 0) sender.sendMessage(list.toString());

    return true;
  }
}
