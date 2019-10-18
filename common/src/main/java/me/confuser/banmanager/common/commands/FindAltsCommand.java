package me.confuser.banmanager.common.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.format.TextColor;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.sql.SQLException;
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

        ((CommonPlayer) sender).sendJSONMessage(alts(players));
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

  public static TextComponent alts(List<PlayerData> players) {
    TextComponent.@NonNull Builder message = TextComponent.builder();
    int index = 0;

    for (PlayerData player : players) {
      TextColor colour = TextColor.GREEN;

      if (CommonCommand.getPlugin().getPlayerBanStorage().isBanned(player.getUUID())) {
        PlayerBanData ban = CommonCommand.getPlugin().getPlayerBanStorage().getBan(player.getUUID());

        if (ban.getExpires() == 0) {
          colour = TextColor.RED;
        } else {
          colour = TextColor.GOLD;
        }
      } else {
        try {
          if (CommonCommand.getPlugin().getPlayerBanRecordStorage().getCount(player) != 0) {
            colour = TextColor.YELLOW;
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      message.append(player.getName()).color(colour).clickEvent(ClickEvent.runCommand("/bminfo " + player.getName()));

      if (index != players.size() - 1) {
        message.append(", ");
      }

      index++;
    }

    return message.build();
  }
}
