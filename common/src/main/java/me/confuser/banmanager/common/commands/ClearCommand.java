package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

public class ClearCommand extends CommonCommand {

  private static HashSet<String> types = new HashSet<String>() {

    {
      add("banrecords");
      add("baniprecords");
      add("kicks");
      add("muterecords");
      add("notes");
      add("reports");
      add("warnings");
    }
  };

  public ClearCommand(BanManagerPlugin plugin) {
    super(plugin, "bmclear", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length == 0) return false;

    // Check if UUID vs name
    final String playerName = parser.args[0];

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, playerName, true);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      ArrayList<String> types = new ArrayList<>();

      if (parser.args.length == 1) {
        // Clear everything
        for (String type : ClearCommand.types) {
          if (!sender.hasPermission("bm.command.clear." + type)) {
            Message.get("sender.error.noPermission").sendTo(sender);
            return;
          }
        }
        types.addAll(ClearCommand.types);
      } else if (parser.args.length == 2) {
        String type = parser.args[1].toLowerCase();

        if (!ClearCommand.types.contains(type)) {
          Message.get("bmclear.error.invalid").sendTo(sender);
          return;
        } else if (sender.hasPermission("bm.command.clear." + type)) {
          types.add(type);
        }
      }

      for (String type : types) {
        try {
          switch (type) {
            case "banrecords":
              getPlugin().getPlayerBanRecordStorage().deleteAll(player);
              break;

            case "baniprecords":
              getPlugin().getIpBanRecordStorage().deleteAll(player.getIp());
              break;

            case "kicks":
              getPlugin().getPlayerKickStorage().deleteAll(player);
              break;

            case "muterecords":
              getPlugin().getPlayerMuteRecordStorage().deleteAll(player);
              break;

            case "notes":
              getPlugin().getPlayerNoteStorage().deleteAll(player);
              break;

            case "reports":
              getPlugin().getPlayerReportStorage().deleteAll(player);
              break;

            case "warnings":
              getPlugin().getPlayerWarnStorage().deleteAll(player);
              break;
          }
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        Message.get("bmclear.notify")
               .set("type", type)
               .set("player", player.getName())
               .set("playerId", player.getUUID().toString())
               .sendTo(sender);
      }
    });

    return true;
  }

}
