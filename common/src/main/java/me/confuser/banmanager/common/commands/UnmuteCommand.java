package me.confuser.banmanager.common.commands;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.parsers.UnbanCommandParser;

import java.sql.SQLException;
import java.util.UUID;

public class UnmuteCommand extends CommonCommand {

  public UnmuteCommand(BanManagerPlugin plugin) {
    super(plugin, "unmute", true, UnbanCommandParser.class, 1);
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
    boolean isMuted;

    if (isUUID) {
      try {
        isMuted = getPlugin().getPlayerMuteStorage().isMuted(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return true;
      }
    } else {
      isMuted = getPlugin().getPlayerMuteStorage().isMuted(playerName);
    }

    if (!isMuted) {
      Message message = Message.get("unmute.error.noExists");
      message.set("player", playerName);

      sender.sendMessage(message.toString());
      return true;
    }

    final String reason = parser.getReason().getMessage();

    getPlugin().getScheduler().runAsync(new Runnable() {

      @Override
      public void run() {
        final PlayerMuteData mute;

        if (isUUID) {
          mute = getPlugin().getPlayerMuteStorage().getMute(UUID.fromString(playerName));
        } else {
          mute = getPlugin().getPlayerMuteStorage().getMute(playerName);
        }

        if (mute == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString
                  ());
          return;
        }

        final PlayerData actor = sender.getData();

        //TODO refactor if async perm check is problem
        if (!actor.getUUID().equals(mute.getActor().getUUID()) && !sender.hasPermission("bm.exempt.override.mute")
                && sender.hasPermission("bm.command.unmute.own")) {
          Message.get("unmute.error.notOwn").set("player", mute.getPlayer().getName()).sendTo(sender);
          return;
        }

        boolean unmuted;

        try {
          unmuted = getPlugin().getPlayerMuteStorage().unmute(mute, actor, reason, isDelete);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!unmuted) {
          return;
        }

        Message message = Message.get("unmute.notify");
        message
                .set("player", mute.getPlayer().getName())
                .set("playerId", mute.getPlayer().getUUID().toString())
                .set("actor", actor.getName())
                .set("reason", reason);

        if (!sender.hasPermission("bm.notify.unmute")) {
          message.sendTo(sender);
        }

        getPlugin().getServer().broadcast(message.toString(), "bm.notify.unmute");

        getPlugin().getScheduler().runSync(() -> {
          CommonPlayer onlinePlayer = getPlugin().getServer().getPlayer(mute.getPlayer().getUUID());

         if (onlinePlayer == null) return;
          if (onlinePlayer.hasPermission("bm.notify.unmute")) return;

          Message.get("unmute.player")
                 .set("displayName", onlinePlayer.getDisplayName())
                 .set("player", mute.getPlayer().getName())
                 .set("playerId", mute.getPlayer().getUUID().toString())
                 .set("reason", mute.getReason())
                 .set("actor", actor.getName())
                 .sendTo(onlinePlayer);

        });
      }

    });

    return true;
  }
}
