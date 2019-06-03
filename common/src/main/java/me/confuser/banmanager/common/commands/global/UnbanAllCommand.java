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
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.global.GlobalPlayerBanRecordData;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class UnbanAllCommand extends SingleCommand {

  public UnbanAllCommand(LocaleManager locale) {
    super(CommandSpec.UNBANALL.localize(locale), "unbanall", CommandPermission.UNBANALL, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() < 1) {
      return CommandResult.INVALID_ARGS;
    }

    // Check if UUID vs name
    final String playerName = args.get(0);
    final boolean isUUID = playerName.length() > 16;
    boolean isBanned;

    if (isUUID) {
      isBanned = plugin.getPlayerBanStorage().isBanned(UUID.fromString(playerName));
    } else {
      isBanned = plugin.getPlayerBanStorage().isBanned(playerName);
    }

    if (!isBanned) {
      Message.UNBAN_ERROR_NOEXISTS.send(sender, "player", playerName);
      return CommandResult.SUCCESS;
    }

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

              GlobalPlayerBanRecordData record = new GlobalPlayerBanRecordData(ban.getPlayer(), actor);

              int unbanned;

              try {
                unbanned = plugin.getGlobalPlayerBanRecordStorage().create(record);
              } catch (SQLException e) {
                Message.SENDER_ERROR_EXCEPTION.send(sender);
                e.printStackTrace();
                return;
              }

              if (unbanned == 0) {
                return;
              }

              Message.UNBANALL_NOTIFY.send(sender,
                     "actor", actor.getName(),
                     "player", ban.getPlayer().getName(),
                     "playerId", ban.getPlayer().getUUID().toString());
            });

    return CommandResult.SUCCESS;
  }


}
