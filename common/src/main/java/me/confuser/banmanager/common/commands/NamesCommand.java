package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerNameSummary;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.kyori.text.event.ClickEvent;
import me.confuser.banmanager.common.kyori.text.format.NamedTextColor;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;
import java.util.List;

public class NamesCommand extends CommonCommand {

  public NamesCommand(BanManagerPlugin plugin) {
    super(plugin, "bmnames", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length < 1) {
      return false;
    }

    final String playerName = parser.args[0];

    if (playerName.length() > 16) {
      Message message = Message.get("sender.error.invalidPlayer");
      message.set("player", playerName);
      sender.sendMessage(message.toString());
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      PlayerData player = getPlugin().getPlayerStorage().retrieve(playerName, true);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      List<PlayerNameSummary> names;
      try {
        names = getPlugin().getPlayerHistoryStorage().getNamesSummary(player);
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
        return;
      }

      sender.sendMessage(Message.get("names.header").set("player", player.getName()).toString());

      if (names.isEmpty()) {
        sender.sendMessage(Message.get("names.none").toString());
        return;
      }

      String dateTimeFormat = Message.getString("names.dateTimeFormat");

      if (!sender.isConsole()) {
        ((CommonPlayer) sender).sendJSONMessage(buildNamesComponent(names, dateTimeFormat));
      } else {
        for (PlayerNameSummary nameData : names) {
          sender.sendMessage(Message.get("names.row")
              .set("name", nameData.getName())
              .set("firstSeen", DateUtils.format(dateTimeFormat, nameData.getFirstSeen()))
              .set("lastSeen", DateUtils.format(dateTimeFormat, nameData.getLastSeen()))
              .toString());
        }
      }
    });

    return true;
  }

  private TextComponent buildNamesComponent(List<PlayerNameSummary> names, String dateTimeFormat) {
    TextComponent.Builder message = Component.text();
    int index = 0;

    for (PlayerNameSummary nameData : names) {
      String hoverText = Message.get("names.row")
          .set("name", nameData.getName())
          .set("firstSeen", DateUtils.format(dateTimeFormat, nameData.getFirstSeen()))
          .set("lastSeen", DateUtils.format(dateTimeFormat, nameData.getLastSeen()))
          .toString();

      message.append(
          Component.text(nameData.getName())
              .color(NamedTextColor.YELLOW)
              .clickEvent(ClickEvent.runCommand("/bminfo " + nameData.getName()))
              .hoverEvent(Component.text(hoverText)));

      if (index != names.size() - 1) {
        message.append(Component.text(", ").color(NamedTextColor.GRAY));
      }

      index++;
    }

    return message.build();
  }
}
