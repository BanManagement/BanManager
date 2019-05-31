package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

public class DeleteCommand extends BukkitCommand<BanManager> {

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

  public DeleteCommand() {
    super("bmdelete");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 2) return false;

    final String type = args[0];

    if (!DeleteCommand.types.contains(type)) {
      Message.get("bmdelete.error.invalid").sendTo(sender);
      return true;
    } else if (!sender.hasPermission("bm.command.delete." + type)) {
      Message.get("sender.error.noPermission").sendTo(sender);
      return true;
    }

    final ArrayList<Integer> ids = new ArrayList<>();

    for (int i = 1; i < args.length; i++) {
      try {
        ids.add(Integer.parseInt(args[i]));
      } catch (NumberFormatException e) {
        Message.get("bmdelete.error.invalidId").set("id", args[i]).sendTo(sender);
        return true;
      }
    }

    if (ids.size() == 0) return false;

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        int rows = 0;

        try {
          switch (type) {
            case "banrecords":
              rows = plugin.getPlayerBanRecordStorage().deleteIds(ids);
              break;

            case "kicks":
              rows = plugin.getPlayerKickStorage().deleteIds(ids);
              break;

            case "muterecords":
              rows = plugin.getPlayerMuteRecordStorage().deleteIds(ids);
              break;

            case "notes":
              rows = plugin.getPlayerNoteStorage().deleteIds(ids);
              break;

            case "reports":
              rows = plugin.getPlayerReportStorage().deleteIds(ids);
              break;

            case "warnings":
              rows = plugin.getPlayerWarnStorage().deleteIds(ids);
              break;
          }
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }


        Message.get("bmdelete.notify").set("rows", rows).sendTo(sender);
      }
    });


    return true;
  }

}
