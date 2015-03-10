package me.confuser.banmanager.commands;

import com.j256.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.UUIDUtils;
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
      add("muterecords");
      add("kicks");
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
          } catch (SQLException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }
        } else {
          player = plugin.getPlayerStorage().retrieve(playerName, true);
        }

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        ArrayList<String> types = new ArrayList<>();

        if (args.length == 1) {
          // Clear everything
          types.addAll(ClearCommand.types);
        } else if (args.length == 2) {
          if (!ClearCommand.types.contains(args[1].toLowerCase())) {
            Message.get("bmclear.error.invalid").sendTo(sender);
            return;
          } else {
            types.add(args[1].toLowerCase());
          }
        }

        for (String type : types) {
          try {
            DeleteBuilder builder = null;
            switch (type) {
              case "banrecords":
                builder = plugin.getPlayerBanRecordStorage().deleteBuilder();

                break;

              case "muterecords":
                builder = plugin.getPlayerMuteRecordStorage().deleteBuilder();

                break;

              case "kicks":
                builder = plugin.getPlayerKickStorage().deleteBuilder();
                break;

              case "warnings":
                builder = plugin.getPlayerWarnStorage().deleteBuilder();
                break;
            }

            builder.where().eq("player_id", player);
            builder.delete();
          } catch (SQLException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }

          Message.get("bmclear.notify").set("type", type).set("player", player.getName()).sendTo(sender);
        }
      }
    });

    return true;
  }

}
