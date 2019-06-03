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
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.parsers.UnbanCommandParser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnbanCommand extends SingleCommand {

  public UnbanCommand(LocaleManager locale) {
    super(CommandSpec.UNBAN.localize(locale), "unban", CommandPermission.UNBAN, Predicates.alwaysFalse());
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
    boolean isBanned;

    if (isUUID) {
      try {
        isBanned = plugin.getPlayerBanStorage().isBanned(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return CommandResult.SUCCESS;
      }
    } else {
      isBanned = plugin.getPlayerBanStorage().isBanned(playerName);
    }

    if (!isBanned) {
      Message.UNBAN_ERROR_NOEXISTS.send(sender, "player", playerName);
      return CommandResult.SUCCESS;
    }

    final String reason = parser.getReason().getMessage();

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      PlayerBanData ban;

      if (isUUID) {
        ban = plugin.getPlayerBanStorage().getBan(UUID.fromString(playerName));
      } else {
        ban = plugin.getPlayerBanStorage().getBan(playerName);
      }

      if (ban == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return;
      }

      final PlayerData actor = CommandUtils.getActor(sender);

      //TODO refactor if async perm check is problem
      if (!actor.getUUID().equals(ban.getActor().getUUID()) && !sender.hasPermission("bm.exempt.override.ban") && sender.hasPermission("bm.command.unban.own")) {
        Message.UNBAN_ERROR_NOTOWN.send(sender, "player", ban.getPlayer().getName());
        return;
      }

      boolean unbanned;

      try {
        unbanned = plugin.getPlayerBanStorage().unban(ban, actor, reason, isDelete);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (!unbanned) {
        return;
      }

      String message = Message.UNBAN_NOTIFY.asString(plugin.getLocaleManager(),
              "player", ban.getPlayer().getName(),
              "playerId", ban.getPlayer().getUUID().toString(),
              "actor", actor.getName(),
              "reason", reason);

      if (!sender.hasPermission("bm.notify.unban")) {
        sender.sendMessage(message);
      }

      CommandUtils.broadcast(message.toString(), "bm.notify.unban");
    });

    return CommandResult.SUCCESS;
  }

  @Override
  public List<String> tabComplete(BanManagerPlugin plugin, Sender sender, List<String> args) {
    ArrayList<String> mostLike = new ArrayList<>();

    if (!sender.hasPermission(this.getPermission().get().getPermission())) return mostLike;
    if (args.size() != 1) return mostLike;

    String nameSearch = args.get(0).toLowerCase();

    for (PlayerBanData ban : plugin.getPlayerBanStorage().getBans().values()) {
      if (ban.getPlayer().getName().toLowerCase().startsWith(nameSearch)) {
        mostLike.add(ban.getPlayer().getName());
      }
    }

    return mostLike;
  }

}
