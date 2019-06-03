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
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.parsers.Reason;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class BanCommand extends SingleCommand {

  public BanCommand(LocaleManager locale) {
    super(CommandSpec.BAN.localize(locale), "ban", CommandPermission.BAN, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> argsIn, String label) {

    CommandParser parser = new CommandParser((String[]) argsIn.toArray(), 1);
    String[] args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(this.getPermission().get().getPermission() + ".silent")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.SUCCESS;
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
    final boolean isBanned;

    if (isUUID) {
      try {
        isBanned = plugin.getPlayerBanStorage().isBanned(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return CommandResult.FAILURE;
      }
    } else {
      isBanned = plugin.getPlayerBanStorage().isBanned(playerName);
    }

    if (isBanned && !sender.hasPermission("bm.command.ban.override")) {
      Message.BAN_ERROR_EXISTS.send(sender, "player", playerName);
      return CommandResult.FAILURE;
    }

    final Sender onlinePlayer;

    if (isUUID) {
      onlinePlayer = plugin.getBootstrap().getPlayerAsSender(UUID.fromString(playerName)).orElse(null);
    } else {
      onlinePlayer = plugin.getBootstrap().getPlayerAsSender(playerName).orElse(null);
    }

    if (onlinePlayer == null) {
      if (!sender.hasPermission("bm.command.ban.offline")) {
        Message.SENDER_ERROR_OFFLINEPERMISSION.send(sender);
        return CommandResult.NO_PERMISSION;
      }
    } else if (!sender.hasPermission("bm.exempt.override.ban") && onlinePlayer.hasPermission("bm.exempt.ban")) {
      Message.SENDER_ERROR_EXEMPT.send(sender, "player", onlinePlayer.getName());
      return CommandResult.SUCCESS;
    }

    final Reason reason = parser.getReason();

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData player = CommandUtils.getPlayer(sender, playerName);

      if (player == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return;
      }

      if (plugin.getExemptionsConfig().isExempt(player, "ban")) {
        Message.SENDER_ERROR_EXEMPT.send(sender, "player", playerName);
        return;
      }

      final PlayerData actor = CommandUtils.getActor(sender);

      if (actor == null) return;

      if (isBanned) {
        PlayerBanData ban;

        if (isUUID) {
          ban = plugin.getPlayerBanStorage().getBan(UUID.fromString(playerName));
        } else {
          ban = plugin.getPlayerBanStorage().getBan(playerName);
        }

        if (ban != null) {
          try {
            plugin.getPlayerBanStorage().unban(ban, actor);
          } catch (SQLException e) {
            Message.SENDER_ERROR_EXCEPTION.send(sender);
            e.printStackTrace();
            return;
          }
        }
      }

      final PlayerBanData ban = new PlayerBanData(player, actor, reason.getMessage());
      boolean created;

      try {
        created = plugin.getPlayerBanStorage().ban(ban, isSilent);
      } catch (SQLException e) {
        CommandUtils.handlePunishmentCreateException(e, sender, Message.BAN_ERROR_EXISTS.asString(plugin.getLocaleManager(),"player", playerName));
        return;
      }

      if (!created) {
        return;
      }

      CommandUtils.handlePrivateNotes(player, actor, reason);

      plugin.getBootstrap().getScheduler().executeSync(() -> {
        if (onlinePlayer == null) return;

        String kickMessage = Message.BAN_PLAYER_KICK.asString(plugin.getLocaleManager(),
                                     "displayName", onlinePlayer.getDisplayName(),
                                     "player", player.getName(),
                                     "playerId", player.getUUID().toString(),
                                     "reason", ban.getReason(),
                                     "actor", actor.getName());

        onlinePlayer.kick(kickMessage);
      });

    });

    return CommandResult.SUCCESS;
  }


}
