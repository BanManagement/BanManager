package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;
import java.util.UUID;

public class MuteCommand extends CommonCommand {

  public MuteCommand(BanManagerPlugin plugin) {
    super(plugin, "mute", true, 1);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    final boolean isSoft = parser.isSoft();

    if (isSoft && !sender.hasPermission(getPermission() + ".soft")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args.length < 2) {
      return false;
    }

    if (parser.args[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    // Check if UUID vs name
    final String playerName = parser.args[0];
    final boolean isUUID = playerName.length() > 16;
    final boolean isMuted;

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

    if (isMuted && !sender.hasPermission("bm.command.mute.override")) {
      Message message = Message.get("mute.error.exists");
      message.set("player", playerName);

      sender.sendMessage(message.toString());
      return true;
    }

    CommonPlayer onlinePlayer;

    if (isUUID) {
      onlinePlayer = getPlugin().getServer().getPlayer(UUID.fromString(playerName));
    } else {
      onlinePlayer = getPlugin().getServer().getPlayer(playerName);
    }

    if (onlinePlayer == null) {
      if (!sender.hasPermission("bm.command.mute.offline")) {
        sender.sendMessage(Message.getString("sender.error.offlinePermission"));
        return true;
      }
    } else if (!sender.hasPermission("bm.exempt.override.mute") && onlinePlayer.hasPermission("bm.exempt.mute")) {
      Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
      return true;
    }

    final Reason reason = parser.getReason();

    getPlugin().getScheduler().runAsync(new Runnable() {

      @Override
      public void run() {
        final PlayerData player = getPlayer(sender, playerName, true);

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        if (getPlugin().getExemptionsConfig().isExempt(player, "mute")) {
          sender.sendMessage(Message.get("sender.error.exempt").set("player", playerName).toString());
          return;
        }

        PlayerData actor = sender.getData();

        if (isMuted) {
          PlayerMuteData mute;

          if (isUUID) {
            mute = getPlugin().getPlayerMuteStorage().getMute(UUID.fromString(playerName));
          } else {
            mute = getPlugin().getPlayerMuteStorage().getMute(playerName);
          }

          if (mute != null) {
            try {
              getPlugin().getPlayerMuteStorage().unmute(mute, actor);
            } catch (SQLException e) {
              sender.sendMessage(Message.get("sender.error.exception").toString());
              e.printStackTrace();
              return;
            }
          }
        }

        PlayerMuteData mute = new PlayerMuteData(player, actor, reason.getMessage(), isSilent, isSoft);
        boolean created;

        try {
          created = getPlugin().getPlayerMuteStorage().mute(mute);
        } catch (SQLException e) {
          handlePunishmentCreateException(e, sender, Message.get("mute.error.exists").set("player",
              playerName));
          return;
        }

        if (!created) {
          return;
        }

        handlePrivateNotes(player, actor, reason);

        CommonPlayer commonPlayer = getPlugin().getServer().getPlayer(player.getUUID());

        if (isSoft || commonPlayer == null) return;

        Message muteMessage = Message.get("mute.player.disallowed")
            .set("displayName", commonPlayer.getDisplayName())
            .set("player", player.getName())
            .set("playerId", player.getUUID().toString())
            .set("reason", mute.getReason())
            .set("actor", actor.getName());

        commonPlayer.sendMessage(muteMessage.toString());
      }

    });

    return true;
  }

}
