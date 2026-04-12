package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class ReportCommand extends CommonCommand {

  public ReportCommand(BanManagerPlugin plugin) {
    super(plugin, "report", true, 1);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args.length < 2) {
      return false;
    }

    if (parser.isInvalidReason()) {
      Message.get("sender.error.invalidReason")
              .set("reason", parser.getReason().getMessage())
              .sendTo(sender);
      return true;
    }

    final String playerName = parser.args[0];
    final TargetResolver.TargetResult target = TargetResolver.resolveTarget(getPlugin().getServer(), playerName);

    if (target.getStatus() == TargetResolver.TargetStatus.NOT_FOUND) {
      Message.get("sender.error.notFound").set("player", playerName).sendTo(sender);
      return true;
    }

    if (target.getStatus() == TargetResolver.TargetStatus.AMBIGUOUS) {
      Message.get("sender.error.ambiguousPlayer").set("player", playerName).sendTo(sender);
      return true;
    }

    CommonPlayer onlinePlayer = target.getOnlinePlayer();
    final String targetName = target.getResolvedName() == null ? playerName : target.getResolvedName();

    if (targetName.equalsIgnoreCase(sender.getName())
        || (onlinePlayer != null && onlinePlayer.getName().equalsIgnoreCase(sender.getName()))) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    if (target.getStatus() != TargetResolver.TargetStatus.EXACT_ONLINE) {
      if (!sender.hasPermission("bm.command.report.offline")) {
        sender.sendMessage(Message.getString("sender.error.offlinePermission"));
        return true;
      }
    } else if (!sender.hasPermission("bm.exempt.override.report") && onlinePlayer.hasPermission("bm.exempt.report")) {
      Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
      return true;
    }

    final String reason = parser.getReason().getMessage();

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, targetName, false);

      if (player == null) {
        Message.get("sender.error.notFound").set("player", targetName).sendTo(sender);
        return;
      }

      if (getPlugin().getExemptionsConfig().isExempt(player, "report")) {
        Message.get("sender.error.exempt").set("player", targetName).sendTo(sender);
        return;
      }

      try {
        if (getPlugin().getPlayerReportStorage().isRecentlyReported(player, getCooldown())) {
          Message.get("report.error.cooldown").sendTo(sender);
          return;
        }
      } catch (SQLException e) {
        Message.get("sender.error.exception").sendTo(sender);
        getPlugin().getLogger().warning("Failed to execute report command", e);
        return;
      }

      final PlayerData actor = sender.getData();

      if (actor == null) return;

      try {
        PlayerReportData report = new PlayerReportData(player, actor, reason, getPlugin().getReportStateStorage()
            .queryForId(1));
        getPlugin().getPlayerReportStorage().report(report, isSilent);
      } catch (SQLException e) {
        Message.get("sender.error.exception").sendTo(sender);
        getPlugin().getLogger().warning("Failed to execute report command", e);
      }

    });

    return true;
  }
}
