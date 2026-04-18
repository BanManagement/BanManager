package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.PaginatedView;

import java.util.ArrayList;
import java.util.List;

public class BanListCommand extends CommonCommand {

  public BanListCommand(BanManagerPlugin plugin) {
    super(plugin, "banlist", false);
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser parser) {
    if (parser.args.length > 2) return false;

    String type = "players";
    int page = 1;

    if (parser.args.length >= 1) {
      type = parser.args[0];
    }
    if (parser.args.length == 2) {
      try {
        page = Integer.parseInt(parser.args[1]);
      } catch (NumberFormatException e) {
        return false;
      }
    }

    List<Component> items = new ArrayList<>();

    if (type.startsWith("play")) {
      if (!sender.hasPermission(getPermission() + ".players")) {
        sender.sendMessage(Message.getString("sender.error.noPermission"));
        return true;
      }

      for (PlayerBanData ban : getPlugin().getPlayerBanStorage().getBans().values()) {
        items.add(Message.get("banlist.row.player")
            .set("name", ban.getPlayer().getName())
            .set("reason", ban.getReason())
            .resolveComponent());
      }
    } else if (type.startsWith("ipr")) {
      if (!sender.hasPermission(getPermission() + ".ipranges")) {
        sender.sendMessage(Message.getString("sender.error.noPermission"));
        return true;
      }

      for (IpRangeBanData ban : getPlugin().getIpRangeBanStorage().getBans().values()) {
        items.add(Message.get("banlist.row.iprange")
            .set("from_ip", ban.getFromIp().toString())
            .set("to_ip", ban.getToIp().toString())
            .set("reason", ban.getReason())
            .resolveComponent());
      }
    } else if (type.startsWith("ip")) {
      if (!sender.hasPermission(getPermission() + ".ips")) {
        sender.sendMessage(Message.getString("sender.error.noPermission"));
        return true;
      }

      for (IpBanData ban : getPlugin().getIpBanStorage().getBans().values()) {
        items.add(Message.get("banlist.row.ip")
            .set("ip", ban.getIp().toString())
            .set("reason", ban.getReason())
            .resolveComponent());
      }
    } else {
      return false;
    }

    Component header = Message.get("banlist.header").set("bans", items.size()).set("type", type).resolveComponent();
    PaginatedView view = new PaginatedView(items, "/banlist " + type);
    view.send(sender, page, header, null);

    return true;
  }
}
