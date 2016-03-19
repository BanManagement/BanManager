package me.confuser.banmanager.commands.report;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class UnassignSubCommand extends SubCommand<BanManager> {

  public UnassignSubCommand() {
    super("unassign");
  }

  @Override
  public boolean onCommand(final CommandSender sender, final String[] args) {
    if (args.length != 1) return false;

    final int id;

    try {
      id = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      Message.get("report.tp.error.invalidId").set("id", args[0]).sendTo(sender);
      return true;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerReportData data;

        try {
          data = plugin.getPlayerReportStorage().queryForId(id);
        } catch (SQLException e) {
          sender.sendMessage(Message.getString("sender.error.exception"));
          e.printStackTrace();
          return;
        }

        if (data == null) {
          sender.sendMessage(Message.getString("report.error.notFound"));
          return;
        }

        data.setAssignee(null);

        try {
          data.setState(plugin.getReportStateStorage().queryForId(1));
          plugin.getPlayerReportStorage().update(data);
        } catch (SQLException e) {
          sender.sendMessage(Message.getString("sender.error.exception"));
          e.printStackTrace();
          return;
        }

        Message.get("report.unassign.player").sendTo(sender);
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
    return "command.report.unassign";
  }
}
