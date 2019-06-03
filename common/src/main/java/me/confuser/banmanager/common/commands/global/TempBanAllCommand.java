package me.confuser.banmanager.common.commands.global;

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
import me.confuser.banmanager.data.global.GlobalPlayerBanData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

public class TempBanAllCommand extends SingleCommand {

  public TempBanAllCommand(LocaleManager locale) {
    super(CommandSpec.TEMPBANALL.localize(locale), "tempbanall", CommandPermission.TEMPBANALL, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() < 3) {
      return CommandResult.INVALID_ARGS;
    }

    if (args.get(0).equalsIgnoreCase(sender.getName())) {
      Message.SENDER_ERROR_NOSELF.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    // Check if UUID vs name
    final String playerName = args.get(0);
    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(args.get(1), true);
    } catch (Exception e1) {
      Message.TIME_ERROR_INVALID.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    if (plugin.getTimeLimits().isPastLimit(sender, TimeLimitType.PLAYER_BAN, expiresCheck)) {
      Message.TIME_ERROR_LIMIT.send(sender);
      return CommandResult.FAILURE;
    }

    final long expires = expiresCheck;

    final String reason = StringUtils.join(args, " ", 2, args.size());

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData player = CommandUtils.getPlayer(sender, playerName);

      if (player == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return;
      }

      final PlayerData actor = CommandUtils.getActor(sender);

      if (actor == null) return;

      final GlobalPlayerBanData ban = new GlobalPlayerBanData(player, actor, reason, expires);
      int created;

      try {
        created = plugin.getGlobalPlayerBanStorage().create(ban);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (created != 1) {
        return;
      }

      Message.TEMPBANALL_NOTIFY.send(sender,
              "actor", ban.getActorName(),
              "reason", ban.getReason(),
              "expires", DateUtils.getDifferenceFormat(ban.getExpires()),
              "player", player.getName(),
              "playerId", player.getUUID().toString()
              );
    });

    return CommandResult.SUCCESS;
  }

}
