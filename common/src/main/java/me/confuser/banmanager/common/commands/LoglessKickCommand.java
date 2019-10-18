package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

public class LoglessKickCommand extends CommonCommand {

  public LoglessKickCommand(BanManagerPlugin plugin) {
    super(plugin, "nlkick", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    boolean isSilent;

    if (parser.getArgs().length != 1) {
      parser = new CommandParser(getPlugin(), parser.getArgs(), 1);
      isSilent = parser.isSilent();
    } else {
      isSilent = false;
    }

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args.length < 1 || parser.args[0].isEmpty()) {
      return false;
    }

    if (parser.args[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    String playerName = parser.args[0];
    final CommonPlayer player = getPlugin().getServer().getPlayer(playerName);

    if (player == null) {
      Message.get("sender.error.offline")
             .set("player", playerName)
             .sendTo(sender);

      return true;
    } else if (!sender.hasPermission("bm.exempt.override.kick") && player.hasPermission("bm.exempt.kick")) {
      Message.get("sender.error.exempt").set("player", player.getName()).sendTo(sender);
      return true;
    }

    final String reason = parser.args.length > 1 ? parser.getReason().getMessage() : "";

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData actor = sender.getData();
      final Message kickMessage;

      if (reason.isEmpty()) {
        kickMessage = Message.get("kick.player.noReason");
      } else {
        kickMessage = Message.get("kick.player.reason").set("reason", reason);
      }

      kickMessage
              .set("displayName", player.getDisplayName())
              .set("player", player.getName())
              .set("playerId", player.getUniqueId().toString())
              .set("actor", actor.getName());

      getPlugin().getScheduler().runSync((Runnable) () -> {
        player.kick(kickMessage.toString());

        Message message = Message.get(reason.isEmpty() ? "kick.notify.noReason" : "kick.notify.reason");
        message.set("player", player.getName()).set("actor", actor.getName()).set("reason", reason);

        if (isSilent || !sender.hasPermission("bm.notify.kick")) {
          message.sendTo(sender);
        }

        if (!isSilent) getPlugin().getServer().broadcast(message.toString(), "bm.notify.kick");
      });

    });

    return true;
  }

}
