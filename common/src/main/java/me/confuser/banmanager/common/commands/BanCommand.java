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

    if (parser.args[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    // Check if UUID vs name
    final String playerName = parser.args[0];
    final boolean isUUID = isUUID(playerName);
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

    if (isBanned && !sender.hasPermission("bm.command.ban.override")) {
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
      if (!sender.hasPermission("bm.command.ban.offline")) {
        sender.sendMessage(Message.getString("sender.error.offlinePermission"));
        return true;
      }
    } else if (!sender.hasPermission("bm.exempt.override.ban") && onlinePlayer.hasPermission("bm.exempt.ban")) {
      Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, playerName, true);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      if (getPlugin().getExemptionsConfig().isExempt(player, "ban")) {
        sender.sendMessage(Message.get("sender.error.exempt").set("player", playerName).toString());
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

      final PlayerBanData ban = new PlayerBanData(player, actor, parser.getReason().getMessage());
      boolean created;

      try {
        created = getPlugin().getPlayerBanStorage().ban(ban, isSilent);
      } catch (SQLException e) {
        handlePunishmentCreateException(e, sender, Message.get("ban.error.exists").set("player",
                playerName));
        return;
      }

      if (!created) {
        return;
      }

      handlePrivateNotes(player, actor, parser.getReason());

      getPlugin().getScheduler().runSync(() -> {
        if (onlinePlayer == null) return;

        Message kickMessage = Message.get("ban.player.kick")
                                     .set("displayName", onlinePlayer.getDisplayName())
                                     .set("player", player.getName())
                                     .set("playerId", player.getUUID().toString())
                                     .set("reason", ban.getReason())
                                     .set("actor", actor.getName());

        onlinePlayer.kick(kickMessage.toString());
      });
    });

    return true;
  }
}
