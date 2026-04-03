package me.confuser.banmanager.common.commands;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.ActionCommand;
import me.confuser.banmanager.common.configs.TimeLimitType;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.parsers.WarnCommandParser;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

public class TempWarnCommand extends CommonCommand {

  public TempWarnCommand(BanManagerPlugin plugin) {
    super(plugin, "tempwarn", true, WarnCommandParser.class, 2);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser originalParser) {
    final WarnCommandParser parser = (WarnCommandParser) originalParser;
    final String[] parsedArgs = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.getPoints() != 1 && !sender.hasPermission(getPermission() + ".points")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parsedArgs.length < 3) {
      return false;
    }

    if (parser.isInvalidReason()) {
      Message.get("sender.error.invalidReason")
              .set("reason", parser.getReason().getMessage())
              .sendTo(sender);
      return true;
    }

    final String playerName = parsedArgs[0];
    TargetResolver.TargetResult target = TargetResolver.resolveTarget(getPlugin().getServer(), playerName);

    if (target.getStatus() == TargetResolver.TargetStatus.NOT_FOUND) {
      sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
      return true;
    }

    if (target.getStatus() == TargetResolver.TargetStatus.AMBIGUOUS) {
      sender.sendMessage(Message.get("sender.error.ambiguousPlayer").set("player", playerName).toString());
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
      if (!sender.hasPermission("bm.command.tempwarn.offline")) {
        sender.sendMessage(Message.getString("sender.error.offlinePermission"));
        return true;
      }
    } else if (!sender.hasPermission("bm.exempt.override.tempwarn") && onlinePlayer
            .hasPermission("bm.exempt.tempwarn")) {
      Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
      return true;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(parsedArgs[1], true);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    if (getPlugin().getConfig().getTimeLimits().isPastLimit(sender, TimeLimitType.PLAYER_WARN, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;
    final Reason reason = parser.getReason();

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, targetName, true);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", targetName).toString());
        return;
      }

      if (getPlugin().getExemptionsConfig().isExempt(player, "tempwarn")) {
        sender.sendMessage(Message.get("sender.error.exempt").set("player", targetName).toString());
        return;
      }

      try {
        if (getPlugin().getPlayerWarnStorage().isRecentlyWarned(player, getCooldown())) {
          Message.get("warn.error.cooldown").sendTo(sender);
          return;
        }
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        getPlugin().getLogger().warning("Failed to execute tempwarn command", e);
        return;
      }

      final PlayerData actor = sender.getData();

      if (actor == null) return;

      final PlayerWarnData warning = new PlayerWarnData(player, actor, reason.getMessage(), parser
              .getPoints(), onlinePlayer != null, expires);

      boolean created;

      try {
        created = getPlugin().getPlayerWarnStorage().addWarning(warning, isSilent);
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        getPlugin().getLogger().warning("Failed to execute tempwarn command", e);
        return;
      }

      if (!created) {
        return;
      }

      handlePrivateNotes(player, actor, reason);

      if (onlinePlayer != null) {
        Message warningMessage = Message.get("tempwarn.player.warned")
                                        .set("displayName", onlinePlayer.getDisplayName())
                                        .set("player", player.getName())
                                        .set("playerId", player.getUUID().toString())
                                        .set("reason", warning.getReason())
                                        .set("actor", actor.getName())
                                        .set("expires", DateUtils.getDifferenceFormat(warning.getExpires()))
                                        .set("id", warning.getId())
                                        .set("points", parser.getPoints());

        onlinePlayer.sendMessage(warningMessage.toString());
      }

      Message message = Message.get("tempwarn.notify")
                               .set("player", player.getName())
                               .set("playerId", player.getUUID().toString())
                               .set("actor", actor.getName())
                               .set("reason", warning.getReason())
                               .set("expires", DateUtils.getDifferenceFormat(warning.getExpires()))
                               .set("id", warning.getId())
                               .set("points", parser.getPoints());

      if (!sender.hasPermission("bm.notify.tempwarn")) {
        message.sendTo(sender);
      }

      if (!isSilent) getPlugin().getServer().broadcast(message.toString(), "bm.notify.tempwarn");

      final List<ActionCommand> actionCommands;

      try {
        actionCommands = getPlugin().getConfig().getWarningActions().getCommands(player, getPlugin().getPlayerWarnStorage().getPointsCount(player));
      } catch (SQLException e) {
        getPlugin().getLogger().warning("Failed to execute tempwarn command", e);
        return;
      }

      if (actionCommands == null || actionCommands.isEmpty()) {
        return;
      }

      for (final ActionCommand action : actionCommands) {

        getPlugin().getScheduler().runSyncLater(() -> {
          String actionCommand = action.getCommand()
                                       .replace("[player]", player.getName())
                                       .replace("[playerId]", player.getUUID().toString())
                                       .replace("[actor]", actor.getName())
                                       .replace("[reason]", warning.getReason())
                                       .replace("[expires]", parsedArgs[1])
                                       .replace("[id]", String.valueOf(warning.getId()))
                                       .replace("[points]", Double.toString(parser.getPoints()));

          getPlugin().getServer().dispatchCommand(getPlugin().getServer().getConsoleSender(), actionCommand);
        }, Duration.ofMillis(action.getDelay()));
      }
    });

    return true;
  }
}
