package me.confuser.banmanager.common.commands.report;

import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SubCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.List;

public class AssignSubCommand extends SubCommand<String> {

  public AssignSubCommand(LocaleManager locale) {
    super(CommandSpec.REPORTS_ASSIGN.localize(locale), "assign", CommandPermission.REPORTS_ASSIGN, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, String s, List<String> args, String label) {
    if (args.size() == 0) return CommandResult.INVALID_ARGS;
    if (sender.isConsole() && args.size() != 2)
      return CommandResult.INVALID_ARGS;

    if (args.size() != 1 && !sender.hasPermission("bm.command.report.assign.other")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (CommandUtils.isValidNameDelimiter(args.get(0))) {
      CommandUtils.handleMultipleNames(sender, "reports assign", args);
      return CommandResult.SUCCESS;
    }

    final Integer id;

    try {
      id = Integer.parseInt(args.get(0));
    } catch (NumberFormatException e) {
      Message.REPORT_TP_ERROR_INVALIDID.send(sender, "id", args.get(0));
      return CommandResult.SUCCESS;
    }

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerReportData data;

      try {
        data = plugin.getPlayerReportStorage().queryForId(id);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (data == null) {
        Message.REPORT_TP_ERROR_NOTFOUND.send(sender);
        return;
      }

      final PlayerData player;
      if (args.size() == 2) {
        player = plugin.getPlayerStorage().retrieve(args.get(1), false);
      } else {
        try {
          player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(sender));
        } catch (SQLException e) {
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }
      }

      if (player == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender);
        return;
      }

      data.setAssignee(player);

      try {
        data.setState(plugin.getReportStateStorage().queryForId(2));
        plugin.getPlayerReportStorage().update(data);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      Message.REPORT_ASSIGN_PLAYER.send(sender, "id", data.getId(), "player", player.getName());

      plugin.getBootstrap().getScheduler().executeSync(() -> {
        Sender bukkitPlayer = CommandUtils.getSender(player.getUUID());

        if (bukkitPlayer == null) return;

        Message.REPORT_ASSIGN_NOTIFY.send(bukkitPlayer,
               "id", data.getId(),
               "displayName", bukkitPlayer.getDisplayName(),
               "player", player.getName(),
               "playerId", player.getUUID().toString(),
               "reason", data.getReason(),
               "actor", sender.getName());

      });
    });

    return CommandResult.SUCCESS;
  }

  @Override
  public void sendUsage(Sender sender, String label) {
    sender.sendMessage("<id> [player]");
  }

}
