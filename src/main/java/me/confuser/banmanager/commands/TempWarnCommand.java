package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.ActionCommand;
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerWarnData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.parsers.Reason;
import me.confuser.banmanager.util.parsers.WarnCommandParser;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TempWarnCommand extends AutoCompleteNameTabCommand<BanManager> {

  public TempWarnCommand() {
    super("tempwarn");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    final WarnCommandParser parser = new WarnCommandParser(args, 2);
    final String[] parsedArgs = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(command.getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.getPoints() != 1 && !sender.hasPermission(command.getPermission() + ".points")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parsedArgs.length < 3) {
      return false;
    }

    if (CommandUtils.isValidNameDelimiter(parsedArgs[0])) {
      CommandUtils.handleMultipleNames(sender, commandName, parsedArgs);
      return true;
    }

    if (parsedArgs[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    // Check if UUID vs name
    final String playerName = parsedArgs[0];
    final boolean isUUID = playerName.length() > 16;
    Player onlinePlayer;

    if (isUUID) {
      try {
        onlinePlayer = plugin.getServer().getPlayer(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return true;
      }
    } else {
      onlinePlayer = plugin.getServer().getPlayer(playerName);
    }

    if (onlinePlayer == null) {
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

    if (plugin.getConfiguration().getTimeLimits().isPastLimit(sender, TimeLimitType.PLAYER_WARN, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;
    final Reason reason = parser.getReason();

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData player = CommandUtils.getPlayer(sender, playerName);

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        if (plugin.getExemptionsConfig().isExempt(player, "tempwarn")) {
          sender.sendMessage(Message.get("sender.error.exempt").set("player", playerName).toString());
          return;
        }

        try {
          if (plugin.getPlayerWarnStorage().isRecentlyWarned(player)) {
            Message.get("warn.error.cooldown").sendTo(sender);
            return;
          }
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        final PlayerData actor = CommandUtils.getActor(sender);

        if (actor == null) return;

        boolean isOnline = plugin.getServer().getPlayer(player.getUUID()) != null;

        final PlayerWarnData warning = new PlayerWarnData(player, actor, reason.getMessage(), parser.getPoints(), isOnline, expires);

        boolean created;

        try {
          created = plugin.getPlayerWarnStorage().addWarning(warning, isSilent);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!created) {
          return;
        }

        CommandUtils.handlePrivateNotes(player, actor, reason);

        if (isOnline) {
          Player bukkitPlayer = plugin.getServer().getPlayer(player.getUUID());

          Message warningMessage = Message.get("tempwarn.player.warned")
                                          .set("displayName", bukkitPlayer.getDisplayName())
                                          .set("player", player.getName())
                                          .set("playerId", player.getUUID().toString())
                                          .set("reason", warning.getReason())
                                          .set("actor", actor.getName())
                                          .set("expires", DateUtils.getDifferenceFormat(warning.getExpires()))
                                          .set("points", parser.getPoints());

          bukkitPlayer.sendMessage(warningMessage.toString());
        }

        Message message = Message.get("tempwarn.notify")
                                 .set("player", player.getName())
                                 .set("playerId", player.getUUID().toString())
                                 .set("actor", actor.getName())
                                 .set("reason", warning.getReason())
                                 .set("expires", DateUtils.getDifferenceFormat(warning.getExpires()))
                                 .set("points", parser.getPoints());

        if (!sender.hasPermission("bm.notify.tempwarn")) {
          message.sendTo(sender);
        }

        if (!isSilent) CommandUtils.broadcast(message.toString(), "bm.notify.tempwarn");

        final List<ActionCommand> actionCommands;

        try {
          actionCommands = plugin.getConfiguration().getWarningActions()
                                 .getCommand((int) plugin.getPlayerWarnStorage().getPointsCount(player));
        } catch (SQLException e) {
          e.printStackTrace();
          return;
        }

        if (actionCommands == null || actionCommands.isEmpty()) {
          return;
        }

        for (final ActionCommand action : actionCommands) {

          plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {

            @Override
            public void run() {
              String actionCommand = action.getCommand()
                                           .replace("[player]", player.getName())
                                           .replace("[playerId]", player.getUUID().toString())
                                           .replace("[actor]", actor.getName())
                                           .replace("[reason]", warning.getReason())
                                           .replace("[expires]", parsedArgs[1])
                                           .replace("[points]", Integer.toString(parser.getPoints()));

              plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), actionCommand);
            }
          }, action.getDelay());
        }
      }
    });

    return true;
  }
}
