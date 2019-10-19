package me.confuser.banmanager.common.commands.report;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.commands.CommonSubCommand;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;

public class AssignSubCommand extends CommonSubCommand {

  public AssignSubCommand(BanManagerPlugin plugin) {
    super(plugin, "assign");
  }

  @Override
  public boolean onCommand(final CommonSender sender, final CommandParser parser) {
    if (parser.getArgs().length == 0) return false;
    if (sender.isConsole() && parser.getArgs().length != 2) return false;

    if (parser.getArgs().length != 1 && !sender.hasPermission("bm.command.report.assign.other")) {
      Message.get("sender.error.noPermission").sendTo(sender);
      return true;
    }

    final Integer id;

    try {
      id = Integer.parseInt(parser.getArgs()[0]);
    } catch (NumberFormatException e) {
      Message.get("report.tp.error.invalidId").set("id", parser.getArgs()[0]).sendTo(sender);
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerReportData data;

      try {
        data = getPlugin().getPlayerReportStorage().queryForId(id);
      } catch (SQLException e) {
        sender.sendMessage(Message.getString("sender.error.exception"));
        e.printStackTrace();
        return;
      }

      if (data == null) {
        sender.sendMessage(Message.getString("report.error.notFound"));
        return;
      }

      final PlayerData player;
      if (parser.getArgs().length == 2) {
        player = getPlugin().getPlayerStorage().retrieve(parser.getArgs()[1], false);
      } else {
        try {
          player = getPlugin().getPlayerStorage().queryForId(UUIDUtils.toBytes(sender.getData().getUUID()));
        } catch (SQLException e) {
          sender.sendMessage(Message.getString("sender.error.exception"));
          e.printStackTrace();
          return;
        }
      }

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").toString());
        return;
      }

      data.setAssignee(player);

      try {
        data.setState(getPlugin().getReportStateStorage().queryForId(2));
        getPlugin().getPlayerReportStorage().update(data);
      } catch (SQLException e) {
        sender.sendMessage(Message.getString("sender.error.exception"));
        e.printStackTrace();
        return;
      }

      Message.get("report.assign.player")
             .set("id", data.getId())
             .set("player", player.getName())
             .sendTo(sender);

      getPlugin().getScheduler().runSync(new Runnable() {

        @Override
        public void run() {
          CommonPlayer bukkitPlayer = getPlugin().getServer().getPlayer(player.getUUID());

          if (bukkitPlayer == null || !bukkitPlayer.isOnline()) return;

          Message.get("report.assign.notify")
                 .set("id", data.getId())
                 .set("displayName", bukkitPlayer.getDisplayName())
                 .set("player", player.getName())
                 .set("playerId", player.getUUID().toString())
                 .set("reason", data.getReason())
                 .set("actor", sender.getName()).sendTo(bukkitPlayer);

        }
      });
    });

    return true;
  }

  @Override
  public String getHelp() {
    return "<id> [player]";
  }

  @Override
  public String getPermission() {
    return "command.reports.assign";
  }
}
