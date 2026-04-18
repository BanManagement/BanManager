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
import me.confuser.banmanager.common.util.MessageRenderer;

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
      message.sendTo(sender);
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      PlayerData player = getPlugin().getPlayerStorage().retrieve(playerName, true);

      if (player == null) {
        Message.get("sender.error.notFound").set("player", playerName).sendTo(sender);
        return;
      }

      List<PlayerNameSummary> names;
      try {
        names = getPlugin().getPlayerHistoryStorage().getNamesSummary(player);
      } catch (SQLException e) {
        Message.get("sender.error.exception").sendTo(sender);
        getPlugin().getLogger().warning("Failed to execute names command", e);
        return;
      }

      Message.get("names.header").set("player", player.getName()).sendTo(sender);

      if (names.isEmpty()) {
        Message.get("names.none").sendTo(sender);
        return;
      }

      String dateTimeFormat = Message.getRawTemplate("names.dateTimeFormat");

      if (!sender.isConsole()) {
        ((CommonPlayer) sender).sendJSONMessage(buildNamesComponent(names, dateTimeFormat));
      } else {
        for (PlayerNameSummary nameData : names) {
          Message.get("names.row")
              .set("name", nameData.name())
              .set("firstSeen", DateUtils.format(dateTimeFormat, nameData.firstSeen()))
              .set("lastSeen", DateUtils.format(dateTimeFormat, nameData.lastSeen()))
              .sendTo(sender);
        }
      }
    });

    return true;
  }

  static TextComponent buildNamesComponent(List<PlayerNameSummary> names, String dateTimeFormat) {
    TextComponent.Builder message = Component.text();
    boolean hasInteractiveTemplate = Message.getRawTemplate("names.interactive") != null;
    String separatorRaw = Message.getRawTemplate("names.separator");
    MessageRenderer renderer = MessageRenderer.getInstance();
    Component separator = separatorRaw != null ? renderer.render(separatorRaw) : Component.text(", ").color(NamedTextColor.GRAY);

    int index = 0;

    for (PlayerNameSummary nameData : names) {
      if (hasInteractiveTemplate) {
        Component entry = Message.get("names.interactive")
            .set("name", nameData.name())
            .set("firstSeen", DateUtils.format(dateTimeFormat, nameData.firstSeen()))
            .set("lastSeen", DateUtils.format(dateTimeFormat, nameData.lastSeen()))
            .resolveComponent();
        message.append(entry);
      } else {
        String hoverText = Message.get("names.row")
            .set("name", nameData.name())
            .set("firstSeen", DateUtils.format(dateTimeFormat, nameData.firstSeen()))
            .set("lastSeen", DateUtils.format(dateTimeFormat, nameData.lastSeen()))
            .toString();

        message.append(
            Component.text(nameData.name())
                .color(NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.runCommand("/bminfo " + nameData.name()))
                .hoverEvent(Component.text(hoverText)));
      }

      if (index != names.size() - 1) {
        message.append(separator);
      }

      index++;
    }

    return message.build();
  }
}
