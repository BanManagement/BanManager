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
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.parsers.Reason;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TempMuteCommand extends SingleCommand {

  public TempMuteCommand(LocaleManager locale) {
    super(CommandSpec.TEMPMUTE.localize(locale), "tempmute", CommandPermission.TEMPMUTE, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> argsIn, String label) {
    CommandParser parser = new CommandParser((String[]) argsIn.toArray(), 2);
    String[] args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(this.getPermission().get().getPermission() + ".silent")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    final boolean isSoft = parser.isSoft();

    if (isSoft && !sender.hasPermission(this.getPermission().get().getPermission() + ".soft")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (args.length < 3) {
      return CommandResult.INVALID_ARGS;
    }

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, this.getName(), args);
      return true;
    }

    if (args[0].equalsIgnoreCase(sender.getName())) {
      Message.SENDER_ERROR_NOSELF.send(sender);
      return true;
    }

    // Check if UUID vs name
    final String playerName = args[0];
    final boolean isUUID = playerName.length() > 16;
    final boolean isMuted;

    if (isUUID) {
      try {
        isMuted = plugin.getPlayerMuteStorage().isMuted(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return CommandResult.SUCCESS;
      }
    } else {
      isMuted = plugin.getPlayerMuteStorage().isMuted(playerName);
    }

    if (isMuted && !sender.hasPermission("bm.command.tempmute.override")) {
      Message.MUTE_ERROR_EXISTS.send(sender, "player", playerName);
      return CommandResult.SUCCESS;
    }

    Sender onlinePlayer;

    if (isUUID) {
      onlinePlayer = plugin.getBootstrap().getPlayerAsSender(UUID.fromString(playerName)).orElse(null);
    } else {
      onlinePlayer = plugin.getBootstrap().getPlayerAsSender(playerName).orElse(null);
    }

    if (onlinePlayer == null) {
      if (!sender.hasPermission("bm.command.tempmute.offline")) {
        Message.SENDER_ERROR_OFFLINEPERMISSION.send(sender);
        return CommandResult.NO_PERMISSION;
      }
    } else if (!sender.hasPermission("bm.exempt.override.tempmute")
            && onlinePlayer.hasPermission("bm.exempt.tempmute")) {
      Message.SENDER_ERROR_EXEMPT.send(sender, "player", onlinePlayer.getName());
      return CommandResult.SUCCESS;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(args[1], true);
    } catch (Exception e1) {
      Message.TIME_ERROR_INVALID.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    if (plugin.getConfiguration().getTimeLimits().isPastLimit(sender, TimeLimitType.PLAYER_MUTE, expiresCheck)) {
      Message.TIME_ERROR_LIMIT.send(sender);
      return CommandResult.SUCCESS;
    }

    final long expires = expiresCheck;
    final Reason reason = parser.getReason();

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData player = CommandUtils.getPlayer(sender, playerName);

      if (player == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return;
      }

      if (plugin.getExemptionsConfig().isExempt(player, "tempmute")) {
        Message.SENDER_ERROR_EXEMPT.send(sender, "player", playerName);
        return;
      }

      final PlayerData actor = CommandUtils.getActor(sender);

      if (actor == null) return;

      if (isMuted) {
        PlayerMuteData mute;

        if (isUUID) {
          mute = plugin.getPlayerMuteStorage().getMute(UUID.fromString(playerName));
        } else {
          mute = plugin.getPlayerMuteStorage().getMute(playerName);
        }

        if (mute != null) {
          try {
            plugin.getPlayerMuteStorage().unmute(mute, actor);
          } catch (SQLException e) {
            Message.SENDER_ERROR_EXCEPTION.send(sender);
            e.printStackTrace();
            return;
          }
        }
      }

      PlayerMuteData mute = new PlayerMuteData(player, actor, reason.getMessage(), isSoft, expires);
      boolean created;

      try {
        created = plugin.getPlayerMuteStorage().mute(mute, isSilent);
      } catch (SQLException e) {
        CommandUtils.handlePunishmentCreateException(e, sender, Message.MUTE_ERROR_EXISTS.asString(plugin.getLocaleManager(), "player", playerName));
        return;
      }

      if (!created) {
        return;
      }

      CommandUtils.handlePrivateNotes(player, actor, reason);

      Sender bukkitPlayer = CommandUtils.getSender(player.getUUID());

      if (isSoft || bukkitPlayer == null) return;

      Message.TEMPMUTE_PLAYER_DISALLOWED.send(bukkitPlayer,
                                   "displayName", bukkitPlayer.getDisplayName(),
                                   "player", player.getName(),
                                   "playerId", player.getUUID().toString(),
                                   "reason", mute.getReason(),
                                   "actor", actor.getName(),
                                   "expires", DateUtils.getDifferenceFormat(mute.getExpires()));

    });

    return CommandResult.SUCCESS;
  }

}
