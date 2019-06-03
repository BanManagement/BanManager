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
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.parsers.UnbanCommandParser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnmuteCommand extends SingleCommand {

  public UnmuteCommand(LocaleManager locale) {
    super(CommandSpec.UNMUTE.localize(locale), "unmute", CommandPermission.UNMUTE, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> argsIn, String label) {
    UnbanCommandParser parser = new UnbanCommandParser((String[]) argsIn.toArray(), 1);
    String[] args = parser.getArgs();

    final boolean isDelete = parser.isDelete();

    if (isDelete && !sender.hasPermission(this.getPermission().get().getPermission() + ".delete")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (args.length < 1) {
      return CommandResult.INVALID_ARGS;
    }

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, this.getName(), args);
      return CommandResult.SUCCESS;
    }

    // Check if UUID vs name
    final String playerName = args[0];
    final boolean isUUID = playerName.length() > 16;
    boolean isMuted;

    if (isUUID) {
      try {
        isMuted = plugin.getPlayerMuteStorage().isMuted(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return CommandResult.FAILURE;
      }
    } else {
      isMuted = plugin.getPlayerMuteStorage().isMuted(playerName);
    }

    if (!isMuted) {
      Message.UNMUTE_ERROR_NOEXISTS.send(sender, "player", playerName);
      return CommandResult.FAILURE;
    }

    final String reason = parser.getReason().getMessage();

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerMuteData mute;

      if (isUUID) {
        mute = plugin.getPlayerMuteStorage().getMute(UUID.fromString(playerName));
      } else {
        mute = plugin.getPlayerMuteStorage().getMute(playerName);
      }

      if (mute == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return;
      }

      final PlayerData actor = CommandUtils.getActor(sender);

      //TODO refactor if async perm check is problem
      if (!actor.getUUID().equals(mute.getActor().getUUID()) && !sender.hasPermission("bm.exempt.override.mute") && sender.hasPermission("bm.command.unmute.own")) {
        Message.UNMUTE_ERROR_NOTOWN.send(sender, "player", mute.getPlayer().getName());
        return;
      }

      boolean unmuted;

      try {
        unmuted = plugin.getPlayerMuteStorage().unmute(mute, actor, reason, isDelete);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (!unmuted) {
        return;
      }

      String message = Message.UNMUTE_NOTIFY.asString(plugin.getLocaleManager(),
              "player", mute.getPlayer().getName(),
              "playerId", mute.getPlayer().getUUID().toString(),
              "actor", actor.getName(),
              "reason", reason);

      if (!sender.hasPermission("bm.notify.unmute")) {
        sender.sendMessage(message);
      }

      CommandUtils.broadcast(message.toString(), "bm.notify.unmute");

      plugin.getBootstrap().getScheduler().executeSync(() -> {
        Sender bukkitPlayer = CommandUtils.getSender(mute.getPlayer().getUUID());

        if (bukkitPlayer == null) return;
        if (bukkitPlayer.hasPermission("bm.notify.unmute")) return;

        Message.UNMUTE_PLAYER.send(bukkitPlayer,
                "displayName", bukkitPlayer.getDisplayName(),
                "player", mute.getPlayer().getName(),
                "playerId", mute.getPlayer().getUUID().toString(),
                "reason", mute.getReason(),
                "actor", actor.getName());

      });
    });

    return CommandResult.SUCCESS;
  }

  @Override
  public List<String> tabComplete(BanManagerPlugin plugin, Sender sender, List<String> args) {
    ArrayList<String> mostLike = new ArrayList<>();

    if (!sender.hasPermission(this.getPermission().get().getPermission())) return mostLike;
    if (args.size() != 1) return mostLike;

    String nameSearch = args.get(0).toLowerCase();

    for (PlayerMuteData ban : plugin.getPlayerMuteStorage().getMutes().values()) {
      if (ban.getPlayer().getName().toLowerCase().startsWith(nameSearch)) {
        mostLike.add(ban.getPlayer().getName());
      }
    }

    return mostLike;
  }

}
