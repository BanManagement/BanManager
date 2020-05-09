package me.confuser.banmanager.common.commands;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.TimeLimitType;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;
import java.util.UUID;

public class TempMuteCommand extends CommonCommand {

  public TempMuteCommand(BanManagerPlugin plugin) {
    super(plugin, "tempmute", true, 2);
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

    if (parser.args.length < 3) {
      return false;
    }

    if (parser.isInvalidReason()) {
      Message.get("sender.error.invalidReason")
              .set("reason", parser.getReason().getMessage())
              .sendTo(sender);
      return true;
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

    if (isMuted && !sender.hasPermission("bm.command.tempmute.override")) {
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
      if (!sender.hasPermission("bm.command.tempmute.offline")) {
        sender.sendMessage(Message.getString("sender.error.offlinePermission"));
        return true;
      }
    } else if (!sender.hasPermission("bm.exempt.override.tempmute")
            && onlinePlayer.hasPermission("bm.exempt.tempmute")) {
      Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
      return true;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(parser.args[1], true);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    if (getPlugin().getConfig().getTimeLimits().isPastLimit(sender, TimeLimitType.PLAYER_MUTE, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;
    final Reason reason = parser.getReason();

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, playerName, true);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      if (getPlugin().getExemptionsConfig().isExempt(player, "tempmute")) {
        sender.sendMessage(Message.get("sender.error.exempt").set("player", playerName).toString());
        return;
      }

      try {
        if (getPlugin().getPlayerMuteStorage().isRecentlyMuted(player, getCooldown())) {
          Message.get("mute.error.cooldown").sendTo(sender);
          return;
        }
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
        return;
      }

      final PlayerData actor = sender.getData();

      if (actor == null) return;

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

      PlayerMuteData mute = new PlayerMuteData(player, actor, reason.getMessage(), isSilent, isSoft, expires);
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

      CommonPlayer onlinePlayer1 = getPlugin().getServer().getPlayer(player.getUUID());

      if (isSoft || onlinePlayer1 == null) return;

      Message muteMessage = Message.get("tempmute.player.disallowed")
                                   .set("displayName", onlinePlayer1.getDisplayName())
                                   .set("player", player.getName())
                                   .set("playerId", player.getUUID().toString())
                                   .set("reason", mute.getReason())
                                   .set("actor", actor.getName())
                                   .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));

      onlinePlayer1.sendMessage(muteMessage.toString());

    });

    return true;
  }

}
