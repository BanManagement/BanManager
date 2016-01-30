package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.banmanager.util.parsers.Reason;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class TempBanCommand extends AutoCompleteNameTabCommand<BanManager> {

  public TempBanCommand() {
    super("tempban");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    CommandParser parser = new CommandParser(args);
    args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(command.getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (args.length < 3) {
      return false;
    }

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, commandName, args);
      return true;
    }

    if (args[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    // Check if UUID vs name
    final String playerName = args[0];
    final boolean isUUID = playerName.length() > 16;
    final boolean isBanned;

    if (isUUID) {
      try {
        isBanned = plugin.getPlayerBanStorage().isBanned(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return true;
      }
    } else {
      isBanned = plugin.getPlayerBanStorage().isBanned(playerName);
    }

    if (isBanned && !sender.hasPermission("bm.command.tempban.override")) {
      Message message = Message.get("ban.error.exists");
      message.set("player", playerName);

      sender.sendMessage(message.toString());
      return true;
    }

    Player onlinePlayer;

    if (isUUID) {
      onlinePlayer = plugin.getServer().getPlayer(UUID.fromString(playerName));
    } else {
      onlinePlayer = plugin.getServer().getPlayer(playerName);
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
      expiresCheck = DateUtils.parseDateDiff(args[1], true);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    if (plugin.getConfiguration().getTimeLimits().isPastLimit(sender, TimeLimitType.PLAYER_BAN, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;
    final Reason reason = CommandUtils.getReason(2, args);

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData player = CommandUtils.getPlayer(sender, playerName);

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        if (plugin.getExemptionsConfig().isExempt(player, "tempban")) {
          sender.sendMessage(Message.get("sender.error.exempt").set("player", playerName).toString());
          return;
        }

        final PlayerData actor = CommandUtils.getActor(sender);

        if (actor == null) return;

        if (isBanned) {
          PlayerBanData ban;

          if (isUUID) {
            ban = plugin.getPlayerBanStorage().getBan(UUID.fromString(playerName));
          } else {
            ban = plugin.getPlayerBanStorage().getBan(playerName);
          }

          if (ban != null) {
            try {
              plugin.getPlayerBanStorage().unban(ban, actor);
            } catch (SQLException e) {
              sender.sendMessage(Message.get("sender.error.exception").toString());
              e.printStackTrace();
              return;
            }
          }
        }

        final PlayerBanData ban = new PlayerBanData(player, actor, reason.getMessage(), expires);
        boolean created;

        try {
          created = plugin.getPlayerBanStorage().ban(ban, isSilent);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!created) {
          return;
        }

        CommandUtils.handlePrivateNotes(player, actor, reason);

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            Player bukkitPlayer = plugin.getServer().getPlayer(player.getUUID());

            if (bukkitPlayer == null) return;

            Message kickMessage = Message.get("tempban.player.kick")
                                         .set("displayName", bukkitPlayer.getDisplayName())
                                         .set("player", player.getName())
                                         .set("playerId", player.getUUID().toString())
                                         .set("reason", ban.getReason())
                                         .set("actor", actor.getName())
                                         .set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));

            bukkitPlayer.kickPlayer(kickMessage.toString());
          }

        });

      }

    });

    return true;
  }
}
