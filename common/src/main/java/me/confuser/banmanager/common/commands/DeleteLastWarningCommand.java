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
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.UUIDUtils;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class DeleteLastWarningCommand extends SingleCommand {

  public DeleteLastWarningCommand(LocaleManager locale) {
    super(CommandSpec.DWARN.localize(locale), "dwarn", CommandPermission.DWARN, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() < 1) {
      return CommandResult.INVALID_ARGS;
    }

    if (CommandUtils.isValidNameDelimiter(args.get(0))) {
      CommandUtils.handleMultipleNames(sender, this.getName(), args);
      return CommandResult.SUCCESS;
    }

    // Check if UUID vs name
    final String playerName = args.get(0);
    final boolean isUUID = playerName.length() > 16;

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData player;

      if (isUUID) {
        try {
          player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(UUID.fromString(playerName)));
        } catch (Exception e) {
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }
      } else {
        player = plugin.getPlayerStorage().retrieve(playerName, true);
      }

      if (player == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return;
      }

      int updated = 0;
      try {
        updated = plugin.getPlayerWarnStorage().deleteRecent(player);
      } catch (SQLException e) {
        e.printStackTrace();
      }

      if (updated == 0) {
        Message.DWARN_PLAYER_ERROR_NOWARNINGS.send(sender, "player", player.getName());
      } else {
        Message.DWARN_NOTIFY.send(sender, "player", player.getName(), "actor", sender.getName());

        Sender bukkitPlayer = CommandUtils.getSender(player.getUUID());

        if (bukkitPlayer == null) return;

        Message.DWARN_PLAYER_NOTIFY.send(bukkitPlayer,
               "player", player.getName(),
               "playerId", player.getUUID().toString(),
               "actor", sender.getName());
      }
    });

    return CommandResult.SUCCESS;
  }
}
