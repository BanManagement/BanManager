package me.confuser.banmanager.common.commands.report;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.commands.CommonSubCommand;
import me.confuser.banmanager.common.data.PlayerReportLocationData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class TeleportSubCommand extends CommonSubCommand {

  public TeleportSubCommand(BanManagerPlugin plugin) {
    super(plugin, "tp");
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (sender.isConsole()) return false;
    if (parser.getArgs().length != 1) return false;

    final int id;

    try {
      id = Integer.parseInt(parser.getArgs()[0]);
    } catch (NumberFormatException e) {
      Message.get("report.tp.error.invalidId").set("id", parser.getArgs()[0]).sendTo(sender);
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerReportLocationData data;

      try {
        data = getPlugin().getPlayerReportLocationStorage().getByReportId(id);
      } catch (SQLException e) {
        sender.sendMessage(Message.getString("sender.error.exception"));
        e.printStackTrace();
        return;
      }

      if (data == null) {
        Message.get("report.tp.error.notFound").sendTo(sender);
        return;
      }

      final CommonWorld world = getPlugin().getServer().getWorld(data.getWorld());

      if (world == null) {
        Message.get("report.tp.error.worldNotFound").set("world", data.getWorld()).sendTo(sender);
        return;
      }

      String dateTimeFormat = Message.getString("report.tp.dateTimeFormat");

      Message.get("report.tp.notify.report")
          .set("id", data.getReport().getId())
          .set("player", data.getReport().getPlayer().getName())
          .set("actor", data.getReport().getActor().getName())
          .set("reason", data.getReport().getReason())
          .set("created", DateUtils.format(dateTimeFormat, data.getReport().getCreated()))
          .sendTo(sender);

      Message.get("report.tp.notify.location")
          .set("world", data.getWorld())
          .set("x", data.getX())
          .set("y", data.getY())
          .set("z", data.getZ())
          .sendTo(sender);

      getPlugin().getScheduler().runSync(() -> {
        CommonPlayer player = getPlugin().getServer().getPlayer(sender.getName());

        player.teleport(world, data.getX(), data.getY(), data.getZ(), data.getYaw(), data
            .getPitch());
      });
    });

    return true;
  }

  @Override
  public String getHelp() {
    return "<id>";
  }

  @Override
  public String getPermission() {
    return "command.reports.tp";
  }
}
