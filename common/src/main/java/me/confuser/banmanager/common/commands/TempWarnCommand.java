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
import me.confuser.banmanager.configs.ActionCommand;
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerWarnData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.parsers.Reason;
import me.confuser.banmanager.util.parsers.WarnCommandParser;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TempWarnCommand extends SingleCommand {

  public TempWarnCommand(LocaleManager locale) {
    super(CommandSpec.TEMPWARN.localize(locale), "tempwarn", CommandPermission.TEMPWARN, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    final WarnCommandParser parser = new WarnCommandParser((String[]) args.toArray(), 2);
    final String[] parsedArgs = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(this.getPermission().get().getPermission()+ ".silent")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (parser.getPoints() != 1 && !sender.hasPermission(this.getPermission().get().getPermission() + ".points")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (parsedArgs.length < 3) {
      return CommandResult.INVALID_ARGS;
    }

    if (CommandUtils.isValidNameDelimiter(parsedArgs[0])) {
      CommandUtils.handleMultipleNames(sender, this.getName(), parsedArgs);
      return CommandResult.SUCCESS;
    }

    if (parsedArgs[0].equalsIgnoreCase(sender.getName())) {
      Message.SENDER_ERROR_NOSELF.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    // Check if UUID vs name
    final String playerName = parsedArgs[0];
    final boolean isUUID = playerName.length() > 16;
    Sender onlinePlayer;

    if (isUUID) {
      try {
        onlinePlayer = plugin.getBootstrap().getPlayerAsSender(UUID.fromString(playerName)).orElse(null);
      } catch (IllegalArgumentException e) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return CommandResult.SUCCESS;
      }
    } else {
      onlinePlayer = plugin.getBootstrap().getPlayerAsSender(playerName).orElse(null);
    }

    if (onlinePlayer == null) {
      if (!sender.hasPermission("bm.command.tempwarn.offline")) {
        Message.SENDER_ERROR_OFFLINEPERMISSION.send(sender);
        return CommandResult.NO_PERMISSION;
      }
    } else if (!sender.hasPermission("bm.exempt.override.tempwarn") && onlinePlayer.hasPermission("bm.exempt.tempwarn")) {
      Message.SENDER_ERROR_EXEMPT.send(sender, "player", onlinePlayer.getName());
      return CommandResult.SUCCESS;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(parsedArgs[1], true);
    } catch (Exception e1) {
      Message.TIME_ERROR_INVALID.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    if (plugin.getConfiguration().getTimeLimits().isPastLimit(sender, TimeLimitType.PLAYER_WARN, expiresCheck)) {
      Message.TIME_ERROR_LIMIT.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    final long expires = expiresCheck;
    final Reason reason = parser.getReason();

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData player = CommandUtils.getPlayer(sender, playerName);

      if (player == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return;
      }

      if (plugin.getExemptionsConfig().isExempt(player, "tempwarn")) {
        Message.SENDER_ERROR_EXEMPT.send(sender, "player", playerName);
        return;
      }

      try {
        if (plugin.getPlayerWarnStorage().isRecentlyWarned(player)) {
          Message.WARN_ERROR_COOLDOWN.send(sender);
          return;
        }
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      final PlayerData actor = CommandUtils.getActor(sender);

      if (actor == null) return;

      boolean isOnline = CommandUtils.getPlayer(player.getUUID()) != null;

      final PlayerWarnData warning = new PlayerWarnData(player, actor, reason.getMessage(), parser.getPoints(), isOnline, expires);

      boolean created;

      try {
        created = plugin.getPlayerWarnStorage().addWarning(warning, isSilent);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (!created) {
        return;
      }

      CommandUtils.handlePrivateNotes(player, actor, reason);

      if (isOnline) {
        Sender bukkitPlayer = CommandUtils.getSender(player.getUUID());

        Message.TEMPWARN_PLAYER_WARNED.send(bukkitPlayer,
                                        "displayName", bukkitPlayer.getDisplayName(),
                                        "player", player.getName(),
                                        "playerId", player.getUUID().toString(),
                                        "reason", warning.getReason(),
                                        "actor", actor.getName(),
                                        "expires", DateUtils.getDifferenceFormat(warning.getExpires()),
                                        "points", parser.getPoints());
      }

      String message = Message.TEMPWARN_NOTIFY.asString(plugin.getLocaleManager(),
                               "player", player.getName(),
                               "playerId", player.getUUID().toString(),
                               "actor", actor.getName(),
                               "reason", warning.getReason(),
                               "expires", DateUtils.getDifferenceFormat(warning.getExpires()),
                               "points", parser.getPoints());

      if (!sender.hasPermission("bm.notify.tempwarn")) {
        sender.sendMessage(message);
      }

      if (!isSilent) {
        CommandUtils.broadcast(message, "bm.notify.tempwarn");
      }

      final List<ActionCommand> actionCommands;

      try {
        actionCommands = plugin.getConfiguration().getWarningActions().getCommand((int) plugin.getPlayerWarnStorage().getPointsCount(player));
      } catch (SQLException e) {
        e.printStackTrace();
        return;
      }

      if (actionCommands == null || actionCommands.isEmpty()) {
        return;
      }

      for (final ActionCommand action : actionCommands) {

        plugin.getBootstrap().getScheduler().syncLater((Runnable) () -> {
          String actionCommand = action.getCommand()
                                       .replace("[player]", player.getName())
                                       .replace("[playerId]", player.getUUID().toString())
                                       .replace("[actor]", actor.getName())
                                       .replace("[reason]", warning.getReason())
                                       .replace("[expires]", parsedArgs[1])
                                       .replace("[points]", Double.toString(parser.getPoints()));

          plugin.getCommandManager().dispatchCommand(plugin.getBootstrap().getConsoleSender(), actionCommand);
        }, action.getDelay());
      }
    });

    return CommandResult.SUCCESS;
  }

}
