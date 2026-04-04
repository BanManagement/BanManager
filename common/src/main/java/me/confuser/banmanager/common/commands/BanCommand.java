package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;
import java.util.UUID;

public class BanCommand extends CommonCommand {

  public BanCommand(BanManagerPlugin plugin) {
    super(plugin, "ban", true, 1);
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser parser) {
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args.length < 2) {
      return false;
    }

    if (parser.isInvalidReason()) {
      Message.get("sender.error.invalidReason")
              .set("reason", parser.getReason().getMessage())
              .sendTo(sender);
      return true;
    }

    final String playerName = parser.args[0];
    final boolean isUUID = isUUID(playerName);
    final TargetResolver.TargetResult target = TargetResolver.resolveTarget(getPlugin().getServer(), playerName);

    if (target.getStatus() == TargetResolver.TargetStatus.NOT_FOUND) {
      sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
      return true;
    }

    if (target.getStatus() == TargetResolver.TargetStatus.AMBIGUOUS) {
      sender.sendMessage(Message.get("sender.error.ambiguousPlayer").set("player", playerName).toString());
      return true;
    }

    final CommonPlayer onlinePlayer = target.getOnlinePlayer();
    final String targetName = target.getResolvedName() == null ? playerName : target.getResolvedName();

    if (targetName.equalsIgnoreCase(sender.getName())
        || (onlinePlayer != null && onlinePlayer.getName().equalsIgnoreCase(sender.getName()))) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    final boolean isBanned;

    if (isUUID) {
      try {
        isBanned = getPlugin().getPlayerBanStorage().isBanned(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return true;
      }
    } else {
      isBanned = getPlugin().getPlayerBanStorage().isBanned(targetName);
    }

    if (isBanned && !sender.hasPermission("bm.command.ban.override")) {
      Message message = Message.get("ban.error.exists");
      message.set("player", targetName);

      sender.sendMessage(message.toString());
      return true;
    }

    if (target.getStatus() != TargetResolver.TargetStatus.EXACT_ONLINE) {
      if (!sender.hasPermission("bm.command.ban.offline")) {
        sender.sendMessage(Message.getString("sender.error.offlinePermission"));
        return true;
      }
    } else if (!sender.hasPermission("bm.exempt.override.ban") && onlinePlayer.hasPermission("bm.exempt.ban")) {
      Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, targetName, true);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", targetName).toString());
        return;
      }

      if (getPlugin().getExemptionsConfig().isExempt(player, "ban")) {
        sender.sendMessage(Message.get("sender.error.exempt").set("player", targetName).toString());
        return;
      }

      try {
        if (getPlugin().getPlayerBanStorage().isRecentlyBanned(player, getCooldown())) {
          Message.get("ban.error.cooldown").sendTo(sender);
          return;
        }
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        getPlugin().getLogger().warning("Failed to execute ban command", e);
        return;
      }

      final PlayerData actor = sender.getData();

      if (actor == null) return;

      if (isBanned) {
        PlayerBanData ban;

        if (isUUID) {
          ban = getPlugin().getPlayerBanStorage().getBan(UUID.fromString(playerName));
        } else {
          ban = getPlugin().getPlayerBanStorage().getBan(targetName);
        }

        if (ban != null) {
          try {
            getPlugin().getPlayerBanStorage().unban(ban, actor);
          } catch (SQLException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            getPlugin().getLogger().warning("Failed to execute ban command", e);
            return;
          }
        }
      }

      final PlayerBanData ban = new PlayerBanData(player, actor, parser.getReason().getMessage(), isSilent);
      boolean created;

      Message kickMessage = null;
      if (onlinePlayer != null) {
        kickMessage = Message.get("ban.player.kick")
                             .set("displayName", onlinePlayer.getDisplayName())
                             .set("player", player.getName())
                             .set("playerId", player.getUUID().toString())
                             .set("reason", ban.getReason())
                             .set("actor", actor.getName());
      }

      try {
        created = getPlugin().getPlayerBanStorage().ban(ban, false, kickMessage);
      } catch (SQLException e) {
        handlePunishmentCreateException(e, sender, Message.get("ban.error.exists").set("player",
                targetName));
        return;
      }

      if (!created) {
        return;
      }

      handlePrivateNotes(player, actor, parser.getReason());

      if (onlinePlayer != null) {
        final Message finalKickMessage = kickMessage;
        getPlugin().getScheduler().runSync(() -> {
          onlinePlayer.kick(finalKickMessage);
        });
      }
    });

    return true;
  }
}
