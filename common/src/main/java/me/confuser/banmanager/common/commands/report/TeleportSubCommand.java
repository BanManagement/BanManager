package me.confuser.banmanager.common.commands.report;

import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SubCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Location;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.PlayerReportLocationData;
import org.apache.commons.lang3.time.FastDateFormat;

import java.sql.SQLException;
import java.util.List;

public class TeleportSubCommand extends SubCommand<String> {

  public TeleportSubCommand(LocaleManager locale) {
    super(CommandSpec.REPORTS_TP.localize(locale), "tp", CommandPermission.REPORTS_TP, Predicates.alwaysFalse());
  }

  //command.reports.tp

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, String target, List<String> args, String label) {
    if (args.size() != 1) return CommandResult.INVALID_ARGS;

    final int id;

    try {
      id = Integer.parseInt(args.get(0));
    } catch (NumberFormatException e) {
      Message.REPORT_TP_ERROR_INVALIDID.send(sender, args.get(0));
      return CommandResult.INVALID_ARGS;
    }

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerReportLocationData data;

      try {
        data = plugin.getPlayerReportLocationStorage().getByReportId(id);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (data == null) {
        Message.REPORT_TP_ERROR_NOTFOUND.send(sender);
        return;
      }

      boolean worldExists = plugin.getBootstrap().doesWorldExist(data.getWorld());

      if (!worldExists) {
        Message.REPORT_TP_ERROR_WORLDNOTFOUND.send(sender, "world", data.getWorld());
        return;
      }

      String dateTimeFormat = Message.REPORT_TP_DATETIMEFORMAT.getMessage();
      FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

      Message.REPORT_TP_NOTIFY_REPORT.send(sender,
             "id", data.getReport().getId(),
             "player", data.getReport().getPlayer().getName(),
             "actor", data.getReport().getActor().getName(),
             "reason", data.getReport().getReason(),
             "created", dateFormatter.format(data.getReport().getCreated() * 1000L));

      Message.REPORT_TP_NOTIFY_LOCATION.send(sender,
             "world", data.getWorld(),
             "x", data.getX(),
             "y", data.getY(),
             "z", data.getZ());

      plugin.getBootstrap().getScheduler().executeSync(() -> {
        Location location = new Location(data.getWorld(), data.getX(), data.getY(), data.getZ(), data.getYaw(), data.getPitch());

        // Teleport safety checks TODO this doesn't seem right?
        if (sender.isInsideVehicle())
          sender.leaveVehicle();

        sender.teleport(location);
      });
    });

    return CommandResult.SUCCESS;
  }

  @Override
  public void sendUsage(Sender sender, String label) {
    sender.sendMessage("<id>");
  }

}
