package me.confuser.banmanager.common.commands.utils;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.commands.CommonSubCommand;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.format.TextColor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DuplicatesSubCommand extends CommonSubCommand {
  public DuplicatesSubCommand(BanManagerPlugin plugin) {
    super(plugin, "duplicates");
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser parser) {
    if (sender.isConsole()) {
      return false;
    }

    if (parser.getArgs().length == 1 || parser.getArgs().length > 2) {
      return false;
    }

    getPlugin().getScheduler().runAsync(() -> {
      if (parser.getArgs().length == 0) {
        HashMap<String, Map.Entry<Integer, List<PlayerData>>> duplicates = getPlugin()
            .getPlayerStorage()
            .getDuplicateNames();

        if (duplicates.size() == 0) {
          Message.get("bmutils.duplicates.lookup.notFound").sendTo(sender);
        }

        duplicates.forEach((key, value) -> {
          TextComponent.Builder message = TextComponent.builder();
          int count = value.getKey();

          message
              .color(TextColor.GOLD)
              .append("[")
              .append(key).append("] * " + count + " ")
              .color(TextColor.GREEN);

          int[] idx = {1};

          value.getValue()
              .forEach(player -> {
                String newName = "newName";

                if (getPlugin().getConfig().isOnlineMode()) {
                  try {
                    newName = UUIDUtils.getCurrentName(getPlugin(), player.getUUID());
                  } catch (Exception ignored) {
                  }
                }

                String cmd = "/bmutils duplicates " + player.getUUID().toString() + " " + newName;

                message
                    .append(
                        TextComponent.builder("[" + idx[0]++ + "] ").
                            clickEvent(ClickEvent.suggestCommand(cmd)));
              });

          ((CommonPlayer) sender).sendJSONMessage(message.build());
        });
      } else if (parser.getArgs().length == 2) {
        UUID id = UUID.fromString(parser.getArgs()[0]);
        String newName = parser.getArgs()[1];

        if (!newName.matches("/[^a-z0-9_]{2,16}/i")) {
          Message.get("bmutils.duplicates.error.invalidName").sendTo(sender);
          return;
        }

        try {
          PlayerData player = getPlugin().getPlayerStorage().queryForId(UUIDUtils.toBytes(id));

          if (player == null) {
            sender.sendMessage(Message.get("sender.error.notFound").set("player", parser.getArgs()[0]));
            return;
          }

          if (getPlugin().getPlayerStorage().retrieve(newName).size() != 0) {
            sender.sendMessage(Message.get("bmutils.duplicates.error.nameExists"));
            return;
          }

          player.setName(newName);

          getPlugin().getPlayerStorage().update(player);

          sender.sendMessage(Message.get("bmutils.duplicates.success").set("player", newName));
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception"));
          e.printStackTrace();
          return;
        }
      }
    });

    return true;
  }

  @Override
  public String getHelp() {
    return "/bmutils duplicates [UUID] [newName]";
  }

  @Override
  public String getPermission() {
    return "command.bmutils.duplicates";
  }
}
