package me.confuser.banmanager.common.commands;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.ActionCommand;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.parsers.WarnCommandParser;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class WarnCommand extends CommonCommand {

  public WarnCommand(BanManagerPlugin plugin) {
    super(plugin, "warn", true, WarnCommandParser.class, 1);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser originalParser) {
    final WarnCommandParser parser = (WarnCommandParser) originalParser;
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.getPoints() != 1 && !sender.hasPermission(getPermission() + ".points")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args.length < 2) {
      return false;
    }

    if (parser.args[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    // Check if UUID vs name
    final String playerName = parser.args[0];
    final boolean isUUID = playerName.length() > 16;
    final Reason reason = parser.getReason();

    CommonPlayer onlinePlayer;

    if (isUUID) {
      try {
        onlinePlayer = getPlugin().getServer().getPlayer(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return true;
      }
    } else {
      onlinePlayer = getPlugin().getServer().getPlayer(playerName);
    }

    if (onlinePlayer == null) {
      if (!sender.hasPermission("bm.command.warn.offline")) {
        sender.sendMessage(Message.getString("sender.error.offlinePermission"));
        return true;
      }
    } else if (!sender.hasPermission("bm.exempt.override.warn") && onlinePlayer.hasPermission("bm.exempt.warn")) {
      Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, playerName, true);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      if (getPlugin().getExemptionsConfig().isExempt(player, "warn")) {
        sender.sendMessage(Message.get("sender.error.exempt").set("player", playerName).toString());
        return;
      }

      try {
        if (getPlugin().getPlayerWarnStorage().isRecentlyWarned(player)) {
          Message.get("warn.error.cooldown").sendTo(sender);
          return;
        }
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
        return;
      }

      final PlayerData actor = sender.getData();

      if (actor == null) return;

      final PlayerWarnData warning = new PlayerWarnData(player, actor, reason.getMessage(), parser
          .getPoints(), onlinePlayer != null);

      boolean created;

      try {
        created = getPlugin().getPlayerWarnStorage().addWarning(warning, isSilent);
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
        return;
      }

      if (!created) {
        return;
      }

      handlePrivateNotes(player, actor, reason);

      if (onlinePlayer != null) {
        Message warningMessage = Message.get("warn.player.warned")
            .set("displayName", onlinePlayer.getDisplayName())
            .set("player", player.getName())
            .set("playerId", player.getUUID().toString())
            .set("reason", warning.getReason())
            .set("actor", actor.getName())
            .set("points", parser.getPoints());

        onlinePlayer.sendMessage(warningMessage.toString());
      }

      Message message = Message.get("warn.notify")
          .set("player", player.getName())
          .set("playerId", player.getUUID().toString())
          .set("actor", actor.getName())
          .set("reason", warning.getReason())
          .set("points", parser.getPoints());

      if (!sender.hasPermission("bm.notify.warn")) {
        message.sendTo(sender);
      }

      if (!isSilent) getPlugin().getServer().broadcast(message.toString(), "bm.notify.warn");

      final List<ActionCommand> actionCommands;

      try {
        actionCommands = getPlugin().getConfig().getWarningActions()
            .getCommand(getPlugin().getPlayerWarnStorage().getPointsCount(player));
      } catch (SQLException e) {
        e.printStackTrace();
        return;
      }

      if (actionCommands == null || actionCommands.isEmpty()) {
        return;
      }

      for (final ActionCommand action : actionCommands)
        getPlugin().getScheduler().runSyncLater(() -> {
          String actionCommand = action.getCommand()
              .replace("[player]", player.getName())
              .replace("[playerId]", player.getUUID().toString())
              .replace("[actor]", actor.getName())
              .replace("[reason]", warning.getReason())
              .replace("[points]", Double.toString(parser.getPoints()));

          getPlugin().getServer().dispatchCommand(getPlugin().getServer().getConsoleSender(), actionCommand);
        }, action.getDelay());
    });

    return true;
  }
}