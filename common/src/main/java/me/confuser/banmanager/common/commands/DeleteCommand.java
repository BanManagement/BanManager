package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

public class DeleteCommand extends CommonCommand {

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

  public DeleteCommand(BanManagerPlugin plugin) {
    super(plugin, "bmdelete", false);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length < 2) return false;

    final String type = parser.args[0];

    if (!types.contains(type)) {
      Message.get("bmdelete.error.invalid").sendTo(sender);
      return true;
    } else if (!sender.hasPermission("bm.command.delete." + type)) {
      Message.get("sender.error.noPermission").sendTo(sender);
      return true;
    }

    final ArrayList<Integer> ids = new ArrayList<>();

    for (int i = 1; i < parser.args.length; i++) {
      try {
        ids.add(Integer.parseInt(parser.args[i]));
      } catch (NumberFormatException e) {
        Message.get("bmdelete.error.invalidId").set("id", parser.args[i]).sendTo(sender);
        return true;
      }
    }

    if (ids.size() == 0) return false;

    getPlugin().getScheduler().runAsync(new Runnable() {

      @Override
      public void run() {
        int rows = 0;

        try {
          switch (type) {
            case "banrecords":
              rows = getPlugin().getPlayerBanRecordStorage().deleteIds(ids);
              break;

            case "baniprecords":
              rows = getPlugin().getIpBanRecordStorage().deleteIds(ids);
              break;

            case "kicks":
              rows = getPlugin().getPlayerKickStorage().deleteIds(ids);
              break;

            case "muterecords":
              rows = getPlugin().getPlayerMuteRecordStorage().deleteIds(ids);
              break;

            case "notes":
              rows = getPlugin().getPlayerNoteStorage().deleteIds(ids);
              break;

            case "reports":
              rows = getPlugin().getPlayerReportStorage().deleteIds(ids);
              break;

            case "warnings":
              rows = getPlugin().getPlayerWarnStorage().deleteIds(ids);
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
