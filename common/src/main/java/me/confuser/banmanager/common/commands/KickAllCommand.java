package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerKickData;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class KickAllCommand extends CommonCommand {

  public KickAllCommand(BanManagerPlugin plugin) {
    super(plugin, "kickall", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length < 1 || parser.args[0].isEmpty()) {
      return false;
    }

    final boolean isSilent = parser.isSilent();

    if (parser.isInvalidReason()) {
      Message.get("sender.error.invalidReason")
              .set("reason", parser.getReason().getMessage())
              .sendTo(sender);
      return true;
    }

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    final String reason = parser.getReason().getMessage();

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData actor = sender.getData();

      if (actor == null) return;

      CommonPlayer[] onlinePlayers = getPlugin().getServer().getOnlinePlayers();

      if (getPlugin().getConfig().isKickLoggingEnabled()) {
        for (CommonPlayer player : onlinePlayers) {
          if (!sender.hasPermission("bm.exempt.override.kick") && player.hasPermission("bm.exempt.kick")) {
            continue;
          }

          PlayerData playerData = player.getData();

          if (playerData == null) continue;

          PlayerKickData data = new PlayerKickData(playerData, actor, reason);

          try {
            getPlugin().getPlayerKickStorage().addKick(data, isSilent);
          } catch (SQLException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
          }
        }
      }


      getPlugin().getScheduler().runSync(() -> {
        Message message = Message.get(reason.isEmpty() ? "kickall.notify.noReason" : "kickall.notify.reason");
        message.set("actor", actor.getName()).set("reason", reason);

        for (CommonPlayer player : onlinePlayers) {
          if (!sender.hasPermission("bm.exempt.override.kick") && player.hasPermission("bm.exempt.kick")) {
            continue;
          }

          Message kickMessage;

          if (reason.isEmpty()) {
            kickMessage = Message.get("kickall.player.noReason");
          } else {
            kickMessage = Message.get("kickall.player.reason").set("reason", reason);
          }

          kickMessage
              .set("displayName", player.getDisplayName())
              .set("player", player.getName())
              .set("playerId", player.getUniqueId().toString())
              .set("actor", actor.getName());

          player.kick(kickMessage.toString());
        }

        if (isSilent || !sender.hasPermission("bm.notify.kick")) {
          message.sendTo(sender);
        }

        if (!isSilent) getPlugin().getServer().broadcast(message.toString(), "bm.notify.kick");
      });
    });

    return true;
  }

}
