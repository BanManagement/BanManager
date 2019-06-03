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
import me.confuser.banmanager.data.PlayerKickData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.List;

public class KickCommand extends SingleCommand {

  public KickCommand(LocaleManager locale) {
    super(CommandSpec.KICK.localize(locale), "kick\"", CommandPermission.KICK, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> argsIn, String label) {
    final boolean isSilent;
    CommandParser parser = null;

    String[] args = (String[]) argsIn.toArray();

    if (args.length != 1) {
      parser = new CommandParser(args, 1);
      args = parser.getArgs();
      isSilent = parser.isSilent();
    } else {
      isSilent = false;
    }

    if (isSilent && !sender.hasPermission(this.getPermission().get().getPermission() + ".silent")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (args.length < 1 || args[0].isEmpty()) {
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

    final String playerName = args[0];
    final Sender player = plugin.getBootstrap().getPlayerAsSender(playerName).orElse(null);

    if (player == null) {
      Message.SENDER_ERROR_OFFLINE.send(sender, "player", playerName);
      return CommandResult.SUCCESS;
    } else if (!sender.hasPermission("bm.exempt.override.kick") && player.hasPermission("bm.exempt.kick")) {
      Message.SENDER_ERROR_EXEMPT.send(sender, "player", player.getName());
      return CommandResult.SUCCESS;
    }

    final String reason = parser != null ? parser.getReason().getMessage() : "";

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData actor = CommandUtils.getActor(sender);

      if (actor == null) return;

      final Message kickMessage;

      if (reason.isEmpty()) {
        kickMessage = Message.KICK_PLAYER_NOREASON;
      } else {
        kickMessage = Message.KICK_PLAYER_REASON;
      }

      String kickStr = kickMessage.asString(plugin.getLocaleManager(),
              "displayName", player.getDisplayName(),
              "player", player.getName(),
              "playerId", UUIDUtils.getUUID(player).toString(),
              "actor", actor.getName(),
              "reason", reason);

      plugin.getBootstrap().getScheduler().executeSync(() -> {
        player.kick(kickStr);

        String message = (reason.isEmpty() ? Message.KICK_NOTIFY_NOREASON : Message.KICK_NOTIFY_REASON).asString(plugin.getLocaleManager(),
                "player", player.getName(), "actor", actor.getName(), "reason", reason);

        if (isSilent || !sender.hasPermission("bm.notify.kick")) {
          sender.sendMessage(message);
        }

        if (!isSilent) CommandUtils.broadcast(message, "bm.notify.kick");
      });

      if (plugin.getConfiguration().isKickLoggingEnabled()) {
        PlayerData player1 = plugin.getPlayerStorage().retrieve(playerName, false);

        if (player1 == null) return;

        PlayerKickData data = new PlayerKickData(player1, actor, reason);

        boolean created;

        try {
          created = plugin.getPlayerKickStorage().addKick(data);
        } catch (SQLException e) {
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }

        if (!created) {
          return;
        }
      }

    });

    return CommandResult.SUCCESS;
  }

}
