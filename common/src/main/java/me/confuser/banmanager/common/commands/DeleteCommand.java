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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DeleteCommand extends SingleCommand {

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

  public DeleteCommand(LocaleManager locale) {
    super(CommandSpec.BMDELETE.localize(locale), "bmdelete", CommandPermission.BMDELETE, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() < 2)
      return CommandResult.INVALID_ARGS;

    final String type = args.get(0);

    if (!DeleteCommand.types.contains(type)) {
      Message.BMDELETE_ERROR_INVALID.send(sender);
      return CommandResult.INVALID_ARGS;
    } else if (!sender.hasPermission("bm.command.delete." + type)) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    final ArrayList<Integer> ids = new ArrayList<>();

    for (int i = 1; i < args.size(); i++) {
      try {
        ids.add(Integer.parseInt(args.get(i)));
      } catch (NumberFormatException e) {
        Message.BMDELETE_ERROR_INVALIDID.send(sender, "id", args.get(i));
        return CommandResult.INVALID_ARGS;
      }
    }

    if (ids.size() == 0)
      return CommandResult.SUCCESS;

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
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
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }


      Message.BMDELETE_NOTIFY.send(sender, "rows", rows);
    });


    return CommandResult.SUCCESS;
  }

}
