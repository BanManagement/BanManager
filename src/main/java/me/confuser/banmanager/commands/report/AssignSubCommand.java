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

public class AssignSubCommand extends SubCommand<BanManager> {

  public AssignSubCommand() {
    super("assign");
  }

  @Override
  public boolean onCommand(final CommandSender sender, final String[] args) {
    if (args.length == 0) return false;
    if (!(sender instanceof Player) && args.length != 2) return false;

    if (args.length != 1 && !sender.hasPermission("bm.command.report.assign.other")) {
      Message.get("sender.error.noPermission").sendTo(sender);
      return true;
    }

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

        final PlayerData player;
        if (args.length == 2) {
          player = plugin.getPlayerStorage().retrieve(args[1], false);
        } else {
          try {
            player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes((Player) sender));
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
          data.setState(plugin.getReportStateStorage().queryForId(2));
          plugin.getPlayerReportStorage().update(data);
        } catch (SQLException e) {
          sender.sendMessage(Message.getString("sender.error.exception"));
          e.printStackTrace();
          return;
        }

        Message.get("report.assign.player")
               .set("id", data.getId())
               .set("player", player.getName())
               .sendTo(sender);

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            Player bukkitPlayer = plugin.getServer().getPlayer(player.getUUID());

            if (bukkitPlayer == null) return;

            Message.get("report.assign.notify")
                   .set("id", data.getId())
                   .set("displayName", bukkitPlayer.getDisplayName())
                   .set("player", player.getName())
                   .set("playerId", player.getUUID().toString())
                   .set("reason", data.getReason())
                   .set("actor", sender.getName()).sendTo(bukkitPlayer);

          }
        });
      }
    });

    return true;
  }

  @Override
  public String getHelp() {
    return "<id> [player]";
  }

  @Override
  public String getPermission() {
    return "command.report.assign";
  }
}
