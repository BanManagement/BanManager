package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;

public class BanListCommand extends CommonCommand {

  public BanListCommand(BanManagerPlugin plugin) {
    super(plugin, "banlist");
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser parser) {
    if (parser.args.length > 1) return false;

    String type = "players";

    if (parser.args.length == 1) {
      type = parser.args[0];
    }

    StringBuilder list = new StringBuilder();
    int total = 0;

    if (type.startsWith("play")) {
      if (!sender.hasPermission(getPermission() + ".players")) {
        sender.sendMessage(Message.getString("sender.error.noPermission"));
        return true;
      }

      for (PlayerBanData ban : getPlugin().getPlayerBanStorage().getBans().values()) {
        list.append(ban.getPlayer().getName());
        list.append(", ");

        total++;
      }
    } else if (type.startsWith("ipr")) {
      if (!sender.hasPermission(getPermission() + ".ipranges")) {
        sender.sendMessage(Message.getString("sender.error.noPermission"));
        return true;
      }

      for (IpRangeBanData ban : getPlugin().getIpRangeBanStorage().getBans().values()) {
        list.append(IPUtils.toString(ban.getFromIp()));
        list.append(" - ");
        list.append(IPUtils.toString(ban.getToIp()));
        list.append(", ");

        total++;
      }
    } else if (type.startsWith("ip")) {
      if (!sender.hasPermission(getPermission() + ".ips")) {
        sender.sendMessage(Message.getString("sender.error.noPermission"));
        return true;
      }

      for (IpBanData ban : getPlugin().getIpBanStorage().getBans().values()) {
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
