package me.confuser.banmanager.commands.report;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportLocationData;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.PlayerSubCommand;
import org.apache.commons.lang.time.FastDateFormat;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class InfoSubCommand extends PlayerSubCommand<BanManager> {

  public InfoSubCommand() {
    super("info");
  }

  @Override
  public boolean onPlayerCommand(final Player player, String[] args) {
    if (args.length != 1) return false;

    final int id;

    try {
      id = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      Message.get("report.info.error.invalidId").set("id", args[0]).sendTo(player);
      return true;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerReportData data;

        try {
          data = plugin.getPlayerReportStorage().queryForId(id);
        } catch (SQLException e) {
          player.sendMessage(Message.getString("sender.error.exception"));
          e.printStackTrace();
          return;
        }

        if (data == null) {
          Message.get("report.info.error.notFound").sendTo(player);
          return;
        }

        String dateTimeFormat = Message.getString("report.info.dateTimeFormat");
        FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

        Message.get("report.info.notify.report")
               .set("id", data.getId())
               .set("player", data.getPlayer().getName())
               .set("actor", data.getActor().getName())
               .set("reason", data.getReason())
               .set("created", dateFormatter.format(data.getCreated() * 1000L))
               .sendTo(player);

        PlayerReportLocationData location;

        try {
          location = plugin.getPlayerReportLocationStorage().getByReportId(id);
        } catch (SQLException e) {
          player.sendMessage(Message.getString("sender.error.exception"));
          e.printStackTrace();
          return;
        }

        if (location == null) return;

        Message.get("report.info.notify.location")
               .set("world", location.getWorld())
               .set("x", location.getX())
               .set("y", location.getY())
               .set("z", location.getZ())
               .sendTo(player);

      }
    });

    return true;
  }

  @Override
  public String getHelp() {
    return "<id>";
  }

  @Override
  public String getPermission() {
    return "command.reports.info";
  }
}
