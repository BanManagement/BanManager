package me.confuser.banmanager.common.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class FindAltsCommand extends CommonCommand {

  public FindAltsCommand(BanManagerPlugin plugin) {
    super(plugin, "alts");
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length < 1) {
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

    getPlugin().getScheduler().runAsync(() -> {
      final long ip;

      if (isName) {
        PlayerData srcPlayer = getPlugin().getPlayerStorage().retrieve(ipStr, false);
        if (srcPlayer == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", ipStr).toString());
          return;
        }

        ip = srcPlayer.getIp();
      } else {
        ip = IPUtils.toLong(ipStr);
      }

      List<PlayerData> players = getPlugin().getPlayerStorage().getDuplicates(ip);

      if (!sender.isConsole()) {
        sender.sendMessage(Message.get("alts.header").set("ip", ipStr).toString());

        if (players.isEmpty()) {
          sender.sendMessage(Message.get("none").toString());
          return;
        }
// @TODO Complete this
//        JSONCommandUtils.alts(players).send((Player) sender);
      } else {
        ArrayList<String> names = new ArrayList<>(players.size());

        for (PlayerData player : players) {
          names.add(player.getName());
        }

        sender.sendMessage(Message.get("alts.header").set("ip", ipStr).toString());

        if (names.isEmpty()) {
          sender.sendMessage(Message.get("none").toString());
          return;
        }

        sender.sendMessage(StringUtils.join(names, ", "));
      }
    });

    return true;
  }
}
