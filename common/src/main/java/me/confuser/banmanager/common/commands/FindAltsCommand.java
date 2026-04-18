package me.confuser.banmanager.common.commands;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.kyori.text.event.ClickEvent;
import me.confuser.banmanager.common.kyori.text.format.NamedTextColor;
import me.confuser.banmanager.common.kyori.text.format.TextColor;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.MessageRenderer;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class FindAltsCommand extends CommonCommand {

  public FindAltsCommand(BanManagerPlugin plugin) {
    super(plugin, "alts", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length < 1) {
      return false;
    }

    final String ipStr = parser.args[0];
    final boolean isName = !IPUtils.isValid(ipStr);

    if (isName && ipStr.length() > 16) {
      Message message = Message.get("sender.error.invalidIp");
      message.set("ip", ipStr);

      message.sendTo(sender);
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      final IPAddress ip = getIp(ipStr);

      if (ip == null) {
        Message.get("alts.header").sendTo(sender);
        Message.get("none").sendTo(sender);
        return;
      }

      List<PlayerData> players = getPlugin().getPlayerStorage().getDuplicatesInTime(ip, getPlugin().getConfig().getTimeAssociatedAlts());

      if (!sender.isConsole()) {
        Message.get("alts.header").set("ip", ipStr).sendTo(sender);

        if (players.isEmpty()) {
          Message.get("none").sendTo(sender);
          return;
        }

        ((CommonPlayer) sender).sendJSONMessage(alts(players));
      } else {
        ArrayList<String> names = new ArrayList<>(players.size());

        for (PlayerData player : players) {
          names.add(player.getName());
        }

        Message.get("alts.header").set("ip", ipStr).sendTo(sender);

        if (names.isEmpty()) {
          Message.get("none").sendTo(sender);
          return;
        }

        sender.sendMessage(String.join(", ", names));
      }
    });

    return true;
  }

  public static TextComponent alts(List<PlayerData> players) {
    TextComponent.Builder message = Component.text();

    List<PlayerData> unbanned = new ArrayList<>();
    Map<UUID, TextColor> colours = new HashMap<>();

    for (PlayerData player : players) {
      PlayerBanData ban = BanManagerPlugin.getInstance().getPlayerBanStorage().getBan(player.getUUID());

      if (ban != null) {
        colours.put(player.getUUID(), ban.getExpires() == 0 ? NamedTextColor.RED : NamedTextColor.GOLD);
      } else {
        unbanned.add(player);
      }
    }

    if (!unbanned.isEmpty()) {
      try {
        Set<UUID> withRecords = BanManagerPlugin.getInstance().getPlayerBanRecordStorage()
            .queryBuilder()
            .selectColumns("player_id")
            .where().in("player_id", unbanned.stream().map(PlayerData::getId).collect(Collectors.toList()))
            .query()
            .stream()
            .map(r -> r.getPlayer().getUUID())
            .collect(Collectors.toSet());

        for (PlayerData player : unbanned) {
          colours.put(player.getUUID(), withRecords.contains(player.getUUID()) ? NamedTextColor.YELLOW : NamedTextColor.GREEN);
        }
      } catch (SQLException e) {
        BanManagerPlugin.getInstance().getLogger().warning("Failed to execute findalts command", e);
        for (PlayerData player : unbanned) {
          colours.put(player.getUUID(), NamedTextColor.GREEN);
        }
      }
    }

    boolean hasEntryTemplate = Message.getRawTemplate("alts.entry") != null;
    String separatorRaw = Message.getRawTemplate("alts.separator");
    MessageRenderer renderer = MessageRenderer.getInstance();
    Component separator = separatorRaw != null ? renderer.render(separatorRaw) : Component.text(", ");

    int index = 0;

    for (PlayerData player : players) {
      TextColor colour = colours.getOrDefault(player.getUUID(), NamedTextColor.GREEN);

      if (hasEntryTemplate) {
        Component entry = Message.get("alts.entry").set("name", player.getName()).resolveComponent();
        message.append(Component.text().color(colour).append(entry));
      } else {
        message.append(
            Component.text(player.getName())
                .color(colour)
                .clickEvent(ClickEvent.runCommand("/bminfo " + player.getName())));
      }

      if (index != players.size() - 1) {
        message.append(separator);
      }

      index++;
    }

    return message.build();
  }
}
