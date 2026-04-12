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

    final boolean isOnlineOnly = parser.isOnlineOnly();

    if (isOnlineOnly && !sender.hasPermission(getPermission() + ".online")) {
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

    final String playerName = parser.args[0];
    final boolean isUUID = playerName.length() > 16;
    TargetResolver.TargetResult target = TargetResolver.resolveTarget(getPlugin().getServer(), playerName);

    if (target.getStatus() == TargetResolver.TargetStatus.NOT_FOUND) {
      Message.get("sender.error.notFound").set("player", playerName).sendTo(sender);
      return true;
    }

    if (target.getStatus() == TargetResolver.TargetStatus.AMBIGUOUS) {
      Message.get("sender.error.ambiguousPlayer").set("player", playerName).sendTo(sender);
      return true;
    }

    CommonPlayer onlinePlayer;
    final String targetName = target.getResolvedName() == null ? playerName : target.getResolvedName();

    onlinePlayer = target.getOnlinePlayer();

    if (targetName.equalsIgnoreCase(sender.getName())
        || (onlinePlayer != null && onlinePlayer.getName().equalsIgnoreCase(sender.getName()))) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    final boolean isMuted;

    if (isUUID) {
      try {
        isMuted = getPlugin().getPlayerMuteStorage().isMuted(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        Message.get("sender.error.notFound").set("player", playerName).sendTo(sender);
        return true;
      }
    } else {
      isMuted = getPlugin().getPlayerMuteStorage().isMuted(targetName);
    }

    if (isMuted && !sender.hasPermission("bm.command.tempmute.override")) {
      Message message = Message.get("mute.error.exists");
      message.set("player", targetName);

      message.sendTo(sender);
      return true;
    }

    if (target.getStatus() != TargetResolver.TargetStatus.EXACT_ONLINE) {
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
      Message.get("time.error.invalid").sendTo(sender);
      return true;
    }

    if (getPlugin().getConfig().getTimeLimits().isPastLimit(sender, TimeLimitType.PLAYER_MUTE, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;
    final Reason reason = parser.getReason();

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, targetName, true);

      if (player == null) {
        Message.get("sender.error.notFound").set("player", targetName).sendTo(sender);
        return;
      }

      if (getPlugin().getExemptionsConfig().isExempt(player, "tempmute")) {
        Message.get("sender.error.exempt").set("player", targetName).sendTo(sender);
        return;
      }

      try {
        if (getPlugin().getPlayerMuteStorage().isRecentlyMuted(player, getCooldown())) {
          Message.get("mute.error.cooldown").sendTo(sender);
          return;
        }
      } catch (SQLException e) {
        Message.get("sender.error.exception").sendTo(sender);
        getPlugin().getLogger().warning("Failed to execute tempmute command", e);
        return;
      }

      final PlayerData actor = sender.getData();

      if (actor == null) return;

      if (isMuted) {
        PlayerMuteData mute;

        if (isUUID) {
          mute = getPlugin().getPlayerMuteStorage().getMute(UUID.fromString(playerName));
        } else {
          mute = getPlugin().getPlayerMuteStorage().getMute(targetName);
        }

        if (mute != null) {
          try {
            getPlugin().getPlayerMuteStorage().unmute(mute, actor);
          } catch (SQLException e) {
            Message.get("sender.error.exception").sendTo(sender);
            getPlugin().getLogger().warning("Failed to execute tempmute command", e);
            return;
          }
        }
      }

      PlayerMuteData mute;
      long now = System.currentTimeMillis() / 1000L;
      long durationSeconds = expires - now;

      if (isOnlineOnly && onlinePlayer == null) {
        mute = new PlayerMuteData(player, actor, reason.getMessage(), isSilent, isSoft, 0, true);
        mute.setPausedRemaining(durationSeconds);
      } else {
        mute = new PlayerMuteData(player, actor, reason.getMessage(), isSilent, isSoft, expires, isOnlineOnly);
      }

      boolean created;

      try {
        created = getPlugin().getPlayerMuteStorage().mute(mute);
      } catch (SQLException e) {
        handlePunishmentCreateException(e, sender, Message.get("mute.error.exists").set("player",
                targetName));
        return;
      }

      if (!created) {
        return;
      }

      handlePrivateNotes(player, actor, reason);

      CommonPlayer onlinePlayer1 = getPlugin().getServer().getPlayer(player.getUUID());

      if (isSoft || onlinePlayer1 == null) return;

      String messageKey = isOnlineOnly ? "tempmute.player.disallowedOnline" : "tempmute.player.disallowed";
      Message muteMessage = Message.get(messageKey)
                                   .set("displayName", onlinePlayer1.getDisplayName())
                                   .set("player", player.getName())
                                   .set("playerId", player.getUUID().toString())
                                   .set("reason", mute.getReason())
                                   .set("actor", actor.getName())
                                   .set("id", mute.getId());

      if (mute.isPaused()) {
        muteMessage.set("expires", DateUtils.formatDifference(mute.getPausedRemaining()));
      } else {
        muteMessage.set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
      }

      muteMessage.sendTo(onlinePlayer1);

    });

    return true;
  }

}
