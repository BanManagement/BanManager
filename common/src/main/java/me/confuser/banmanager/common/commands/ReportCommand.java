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
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ReportCommand extends SingleCommand {

  public ReportCommand(LocaleManager locale) {
    super(CommandSpec.REPORT.localize(locale), "report", CommandPermission.REPORT, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> argsIn, String label) {
    CommandParser parser = new CommandParser(argsIn);
    String[] args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(this.getPermission().get().getPermission() + ".silent")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (args.length < 2) {
      return CommandResult.INVALID_ARGS;
    }

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, this.getName(), args);
      return CommandResult.SUCCESS;
    }

    if (args[0].equalsIgnoreCase(sender.getName())) {
      Message.SENDER_ERROR_NOSELF.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    // Check if UUID vs name
    final String playerName = args[0];
    final boolean isUUID = playerName.length() > 16;

    Sender onlinePlayer;

    if (isUUID) {
      onlinePlayer = plugin.getBootstrap().getPlayerAsSender(UUID.fromString(playerName)).orElse(null);
    } else {
      onlinePlayer = plugin.getBootstrap().getPlayerAsSender(playerName).orElse(null);
    }

    if (onlinePlayer == null) {
      if (!sender.hasPermission("bm.command.report.offline")) {
        Message.SENDER_ERROR_OFFLINEPERMISSION.send(sender);
        return CommandResult.NO_PERMISSION;
      }
    } else if (!sender.hasPermission("bm.exempt.override.report") && onlinePlayer.hasPermission("bm.exempt.report")) {
      Message.SENDER_ERROR_EXEMPT.send(sender, "player", onlinePlayer.getName());
      return CommandResult.SUCCESS;
    }

    final String reason = CommandUtils.getReason(1, args).getMessage();

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData player = CommandUtils.getPlayer(sender, playerName, false);

      if (player == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return;
      }

      if (plugin.getExemptionsConfig().isExempt(player, "ban")) {
        Message.SENDER_ERROR_EXEMPT.send(sender, "player", playerName);
        return;
      }

      try {
        if (plugin.getPlayerReportStorage().isRecentlyReported(player)) {
          Message.REPORT_ERROR_COOLDOWN.send(sender);
          return;
        }
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      final PlayerData actor = CommandUtils.getActor(sender);

      if (actor == null) return;

      try {
        PlayerReportData report = new PlayerReportData(player, actor, reason, plugin.getReportStateStorage().queryForId(1));
        plugin.getPlayerReportStorage().report(report, isSilent);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
      }

    });

    return CommandResult.SUCCESS;
  }
}
