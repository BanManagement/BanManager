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
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.banmanager.util.parsers.Reason;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class MuteCommand extends SingleCommand {

  public MuteCommand(LocaleManager locale) {
    super(CommandSpec.MUTE.localize(locale), "mute", CommandPermission.MUTE, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> argsIn, String label) {
    CommandParser parser = new CommandParser(argsIn, 1);
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

    if (isMuted && !sender.hasPermission("bm.command.mute.override")) {
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
      if (!sender.hasPermission("bm.command.mute.offline")) {
        Message.SENDER_ERROR_OFFLINEPERMISSION.send(sender);
        return CommandResult.NO_PERMISSION;
      }
    } else if (!sender.hasPermission("bm.exempt.override.mute") && onlinePlayer.hasPermission("bm.exempt.mute")) {
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

      if (plugin.getExemptionsConfig().isExempt(player, "mute")) {
        Message.SENDER_ERROR_EXEMPT.send(sender, "player", playerName);
        return;
      }

      PlayerData actor;

      if (!sender.isConsole()) {
        try {
          actor = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(sender));
        } catch (SQLException e) {
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }
      } else {
        actor = plugin.getPlayerStorage().getConsole();
      }

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

      PlayerMuteData mute = new PlayerMuteData(player, actor, reason.getMessage(), isSoft);
      boolean created;

      try {
        created = plugin.getPlayerMuteStorage().mute(mute, isSilent);
      } catch (SQLException e) {
        CommandUtils.handlePunishmentCreateException(e, sender, Message.MUTE_ERROR_EXISTS.asString(plugin.getLocaleManager(),"player", playerName));
        return;
      }

      if (!created) {
        return;
      }

      CommandUtils.handlePrivateNotes(player, actor, reason);

      Sender bukkitPlayer = CommandUtils.getSender(player.getUUID());

      if (isSoft || bukkitPlayer == null) return;

      Message.MUTE_PLAYER_DISALLOWED.send(bukkitPlayer,
                                   "displayName", bukkitPlayer.getDisplayName(),
                                   "player", player.getName(),
                                   "playerId", player.getUUID().toString(),
                                   "reason", mute.getReason(),
                                   "actor", actor.getName());

    });

    return CommandResult.SUCCESS;
  }

}
