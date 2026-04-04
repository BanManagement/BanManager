package me.confuser.banmanager.common.commands;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.parsers.UnbanCommandParser;

import java.sql.SQLException;
import java.util.UUID;

public class UnbanCommand extends CommonCommand {

  public UnbanCommand(BanManagerPlugin plugin) {
    super(plugin, "unban", true, UnbanCommandParser.class, 0);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser originalParser) {
    final UnbanCommandParser parser = (UnbanCommandParser) originalParser;
    final boolean isDelete = parser.isDelete();

    if (isDelete && !sender.hasPermission(getPermission() + ".delete")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args.length < 1) {
      return false;
    }

    // Check if UUID vs name
    final String playerName = parser.args[0];
    final boolean isUUID = playerName.length() > 16;
    boolean isBanned;

    if (isUUID) {
      try {
        isBanned = getPlugin().getPlayerBanStorage().isBanned(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return true;
      }
    } else {
      isBanned = getPlugin().getPlayerBanStorage().isBanned(playerName);
    }

    if (!isBanned) {
      Message message = Message.get("unban.error.noExists");
      message.set("player", playerName);

      sender.sendMessage(message.toString());
      return true;
    }

    final String reason = parser.getReason().getMessage();
    final boolean canOverride = sender.hasPermission("bm.exempt.override.ban");
    final boolean isOwnOnly = sender.hasPermission("bm.command.unban.own");

    getPlugin().getScheduler().runAsync(() -> {
      PlayerBanData ban;

      if (isUUID) {
        ban = getPlugin().getPlayerBanStorage().getBan(UUID.fromString(playerName));
      } else {
        ban = getPlugin().getPlayerBanStorage().getBan(playerName);
      }

      if (ban == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      final PlayerData actor = sender.getData();

      if (!actor.getUUID().equals(ban.getActor().getUUID()) && !canOverride && isOwnOnly) {
        Message.get("unban.error.notOwn").set("player", ban.getPlayer().getName()).sendTo(sender);
        return;
      }

      boolean unbanned;

      try {
        unbanned = getPlugin().getPlayerBanStorage().unban(ban, actor, reason, isDelete, parser.isSilent());
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        getPlugin().getLogger().warning("Failed to execute unban command", e);
        return;
      }

      if (!unbanned) {
        return;
      }

      Message message = Message.get("unban.notify");
      message
              .set("player", ban.getPlayer().getName())
              .set("playerId", ban.getPlayer().getUUID().toString())
              .set("actor", actor.getName())
              .set("id", ban.getId())
              .set("reason", reason);

      if (!sender.hasPermission("bm.notify.unban") || parser.isSilent()) {
        message.sendTo(sender);
      }

      if (!parser.isSilent()) {
        getPlugin().getServer().broadcast(message, "bm.notify.unban");
      }
    });

    return true;
  }
}
