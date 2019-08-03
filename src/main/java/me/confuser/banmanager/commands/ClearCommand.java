package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.common.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class ClearCommand extends AutoCompleteNameTabCommand<BanManager> {

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

  public ClearCommand() {
    super("bmclear");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, final String[] args) {

    if (args.length == 0) return false;

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, commandName, args);
      return true;
    }

    // Check if UUID vs name
    final String playerName = args[0];
    final boolean isUUID = playerName.length() > 16;

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData player;

        if (isUUID) {
          try {
            player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(UUID.fromString(playerName)));
          } catch (Exception e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }
        } else {
          player = plugin.getPlayerStorage().retrieve(playerName, false);
        }

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        ArrayList<String> types = new ArrayList<>();

        if (args.length == 1) {
          // Clear everything
          for (String type : ClearCommand.types) {
            if (!sender.hasPermission("bm.command.clear." + type)) {
              Message.get("sender.error.noPermission").sendTo(sender);
              return;
            }
          }
          types.addAll(ClearCommand.types);
        } else if (args.length == 2) {
          String type = args[1].toLowerCase();

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
      }
    });

    return true;
  }

}
