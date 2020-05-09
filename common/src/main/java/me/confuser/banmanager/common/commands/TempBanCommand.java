package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.TimeLimitType;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;
import java.util.UUID;

public class TempBanCommand extends CommonCommand {

  public TempBanCommand(BanManagerPlugin plugin) {
    super(plugin, "tempban", true, 2);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
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
    final boolean isBanned;

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

    if (isBanned && !sender.hasPermission("bm.command.tempban.override")) {
      Message message = Message.get("ban.error.exists");
      message.set("player", playerName);

      sender.sendMessage(message.toString());
      return true;
    }

    final CommonPlayer onlinePlayer;

    if (isUUID) {
      onlinePlayer = getPlugin().getServer().getPlayer(UUID.fromString(playerName));
    } else {
      onlinePlayer = getPlugin().getServer().getPlayer(playerName);
    }

    if (onlinePlayer == null) {
      if (!sender.hasPermission("bm.command.tempban.offline")) {
        sender.sendMessage(Message.getString("sender.error.offlinePermission"));
        return true;
      }
    } else if (!sender.hasPermission("bm.exempt.override.tempban") && onlinePlayer.hasPermission("bm.exempt.tempban")) {
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

    if (getPlugin().getConfig().getTimeLimits().isPastLimit(sender, TimeLimitType.PLAYER_BAN, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;
    final Reason reason = parser.getReason();

    getPlugin().getScheduler().runAsync(new Runnable() {

      @Override
      public void run() {
        final PlayerData player = getPlayer(sender, playerName, true);

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        if (getPlugin().getExemptionsConfig().isExempt(player, "tempban")) {
          sender.sendMessage(Message.get("sender.error.exempt").set("player", playerName).toString());
          return;
        }

        try {
          if (getPlugin().getPlayerBanStorage().isRecentlyBanned(player, getCooldown())) {
            Message.get("ban.error.cooldown").sendTo(sender);
            return;
          }
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        final PlayerData actor = sender.getData();

        if (actor == null) return;

        if (isBanned) {
          PlayerBanData ban;

          if (isUUID) {
            ban = getPlugin().getPlayerBanStorage().getBan(UUID.fromString(playerName));
          } else {
            ban = getPlugin().getPlayerBanStorage().getBan(playerName);
          }

          if (ban != null) {
            try {
              getPlugin().getPlayerBanStorage().unban(ban, actor);
            } catch (SQLException e) {
              sender.sendMessage(Message.get("sender.error.exception").toString());
              e.printStackTrace();
              return;
            }
          }
        }

        final PlayerBanData ban = new PlayerBanData(player, actor, reason.getMessage(), isSilent, expires);
        boolean created;

        try {
          created = getPlugin().getPlayerBanStorage().ban(ban);
        } catch (SQLException e) {
          handlePunishmentCreateException(e, sender, Message.get("ban.error.exists").set("player",
              playerName));
          return;
        }

        if (!created) {
          return;
        }

        handlePrivateNotes(player, actor, reason);

        getPlugin().getScheduler().runSync(() -> {
          if (onlinePlayer == null) return;

          Message kickMessage = Message.get("tempban.player.kick")
              .set("displayName", onlinePlayer.getDisplayName())
              .set("player", player.getName())
              .set("playerId", player.getUUID().toString())
              .set("reason", ban.getReason())
              .set("actor", actor.getName())
              .set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));

          onlinePlayer.kick(kickMessage.toString());
        });

      }

    });

    return true;
  }
}
