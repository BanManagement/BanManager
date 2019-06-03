package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class ClearCommand extends SingleCommand {

  private static HashSet<String> types = new HashSet<String>() {

    {
      add("banrecords");
      add("kicks");
      add("muterecords");
      add("notes");
      add("reports");
      add("warnings");
    }
  };

  public ClearCommand(LocaleManager locale) {
    super(CommandSpec.BMCLEAR.localize(locale), "bmclear", CommandPermission.BMCLEAR, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() == 0) return CommandResult.INVALID_ARGS;

    if (CommandUtils.isValidNameDelimiter(args.get(0))) {
      CommandUtils.handleMultipleNames(sender, this.getName(), (String[]) args.toArray());
      return CommandResult.SUCCESS;
    }

    // Check if UUID vs name
    final String playerName = args.get(0);
    final boolean isUUID = playerName.length() > 16;

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData player;

      if (isUUID) {
        try {
          player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(UUID.fromString(playerName)));
        } catch (Exception e) {
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }
      } else {
        player = plugin.getPlayerStorage().retrieve(playerName, false);
      }

      if (player == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return;
      }

      ArrayList<String> types = new ArrayList<>();

      if (args.size() == 1) {
        // Clear everything
        for (String type : ClearCommand.types) {
          if (!sender.hasPermission("bm.command.clear." + type)) {
            Message.SENDER_ERROR_NOPERMISSION.send(sender);
            return;
          }
        }
        types.addAll(ClearCommand.types);
      } else if (args.size() == 2) {
        String type = args.get(1).toLowerCase();

        if (!ClearCommand.types.contains(type)) {
          Message.BMCLEAR_ERROR_INVALID.send(sender);
          return;
        } else if (sender.hasPermission("bm.command.clear." + type)) {
          types.add(type);
        }
      }

      for (String type : types) {
        try {
          switch (type) {
            case "banrecords":
              plugin.getPlayerBanRecordStorage().deleteAll(player);
              break;

            case "kicks":
              plugin.getPlayerKickStorage().deleteAll(player);
              break;

            case "muterecords":
              plugin.getPlayerMuteRecordStorage().deleteAll(player);
              break;

            case "notes":
              plugin.getPlayerNoteStorage().deleteAll(player);
              break;

            case "reports":
              plugin.getPlayerReportStorage().deleteAll(player);
              break;

            case "warnings":
              plugin.getPlayerWarnStorage().deleteAll(player);
              break;
          }
        } catch (SQLException e) {
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }

        Message.BMCLEAR_NOTIFY.send(sender,
               "type", type,
               "player", player.getName(),
               "playerId", player.getUUID().toString());
      }
    });

    return CommandResult.SUCCESS;
  }

}
