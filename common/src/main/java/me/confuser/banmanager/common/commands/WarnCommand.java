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
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerWarnData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.parsers.Reason;
import me.confuser.banmanager.util.parsers.WarnCommandParser;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class WarnCommand extends SingleCommand {

  public WarnCommand(LocaleManager locale) {
    super(CommandSpec.WARN.localize(locale), "warn", CommandPermission.WARN, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> argsIn, String label) {
    final WarnCommandParser parser = new WarnCommandParser((String[]) argsIn.toArray(), 1);
    String[] args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(this.getPermission().get().getPermission() + ".silent")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (parser.getPoints() != 1 && !sender.hasPermission(this.getPermission().get().getPermission() + ".points")) {
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
    final Reason reason = parser.getReason();

    Sender onlinePlayer;

    if (isUUID) {
      try {
        onlinePlayer = plugin.getBootstrap().getPlayerAsSender(UUID.fromString(playerName)).orElse(null);
      } catch (IllegalArgumentException e) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return CommandResult.FAILURE;
      }
    } else {
      onlinePlayer = plugin.getBootstrap().getPlayerAsSender(playerName).orElse(null);
    }

    if (onlinePlayer == null) {
      if (!sender.hasPermission("bm.command.warn.offline")) {
        Message.SENDER_ERROR_OFFLINEPERMISSION.send(sender);
        return CommandResult.NO_PERMISSION;
      }
    } else if (!sender.hasPermission("bm.exempt.override.warn") && onlinePlayer.hasPermission("bm.exempt.warn")) {
      Message.SENDER_ERROR_EXEMPT.send(sender, "player", onlinePlayer.getName());
      return CommandResult.SUCCESS;
    }

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData player = CommandUtils.getPlayer(sender, playerName);

      if (player == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return;
      }

      if (plugin.getExemptionsConfig().isExempt(player, "warn")) {
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

      final PlayerWarnData warning = new PlayerWarnData(player, actor, reason.getMessage(), parser.getPoints(), isOnline);

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

        Message.WARN_PLAYER_WARNED.send(bukkitPlayer,
                                        "displayName", bukkitPlayer.getDisplayName(),
                                        "player", player.getName(),
                                        "playerId", player.getUUID().toString(),
                                        "reason", warning.getReason(),
                                        "actor", actor.getName(),
                                        "points", parser.getPoints());

      }

      String message = Message.WARN_NOTIFY.asString(plugin.getLocaleManager(),
                               "player", player.getName(),
                               "playerId", player.getUUID().toString(),
                               "actor", actor.getName(),
                               "reason", warning.getReason(),
                               "points", parser.getPoints());

      if (!sender.hasPermission("bm.notify.warn")) {
        sender.sendMessage(message);
      }

      if (!isSilent) CommandUtils.broadcast(message, "bm.notify.warn");

      final List<ActionCommand> actionCommands;

      try {
        actionCommands = plugin.getConfiguration().getWarningActions().getCommand(plugin.getPlayerWarnStorage().getPointsCount(player));
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
                                       .replace("[points]", Double.toString(parser.getPoints()));

          plugin.getServer().dispatchCommand(plugin.getBootstrap().getConsoleSender(), actionCommand);
        }, action.getDelay());
      }
    });

    return CommandResult.SUCCESS;
  }


}