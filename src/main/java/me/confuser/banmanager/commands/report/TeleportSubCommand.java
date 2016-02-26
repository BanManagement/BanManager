package me.confuser.banmanager.commands.report;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerReportLocationData;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.PlayerSubCommand;
import org.apache.commons.lang.time.FastDateFormat;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class TeleportSubCommand extends PlayerSubCommand<BanManager> {

  public TeleportSubCommand() {
    super("tp");
  }

  @Override
  public boolean onPlayerCommand(final Player player, String[] args) {
    if (args.length != 1) return false;

    final int id;

    try {
      id = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      Message.get("report.tp.error.invalidId").set("id", args[0]).sendTo(player);
      return true;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        PlayerReportLocationData data;

        try {
          data = plugin.getPlayerReportLocationStorage().getByReportId(id);
        } catch (SQLException e) {
          player.sendMessage(Message.getString("sender.error.exception"));
          e.printStackTrace();
          return;
        }

        if (data == null) {
          player.sendMessage(Message.getString("report.tp.error.notFound"));
          return;
        }

        World world = plugin.getServer().getWorld(data.getWorld());

        if (world == null) {
          Message.get("report.tp.error.worldNotFound").set("world", data.getWorld()).sendTo(player);
          return;
        }

        String dateTimeFormat = Message.getString("report.tp.dateTimeFormat");
        FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

        Message.get("report.tp.notify.report")
               .set("player", data.getReport().getPlayer().getName())
               .set("actor", data.getReport().getActor().getName())
               .set("reason", data.getReport().getReason())
               .set("created", dateFormatter
                       .format(data.getReport().getCreated() * 1000L))
               .sendTo(player);

        Message.get("report.tp.notify.location")
               .set("world", data.getWorld())
               .set("x", data.getX())
               .set("y", data.getY())
               .set("z", data.getZ())
               .sendTo(player);

        Location location = new Location(world, data.getX(), data.getY(), data.getZ(), data.getYaw(), data.getPitch());

        // Teleport safety checks
        if (player.isInsideVehicle()) player.leaveVehicle();

        player.teleport(location);
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
    return "command.report.tp";
  }
}
