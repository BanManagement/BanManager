package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import org.apache.commons.lang.StringUtils;
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
    if (args.length < 3) {
      return false;
    }

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, commandName, args);
      return true;
    }

    if (args[0].toLowerCase().equals(sender.getName().toLowerCase())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    // Check if UUID vs name
    final String playerName = args[0];
    final boolean isUUID = playerName.length() > 16;
    boolean isBanned = false;

    if (isUUID) {
      isBanned = plugin.getPlayerBanStorage().isBanned(UUID.fromString(playerName));
    } else {
      isBanned = plugin.getPlayerBanStorage().isBanned(playerName);
    }

    if (isBanned) {
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
    final String reason = StringUtils.join(args, " ", 2, args.length);

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData player;

        if (isUUID) {
          try {
            player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(UUID.fromString(playerName)));
          } catch (SQLException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }
        } else {
          player = plugin.getPlayerStorage().retrieve(playerName, true);
        }

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        final PlayerData actor;

        if (sender instanceof Player) {
          actor = plugin.getPlayerStorage().getOnline((Player) sender);
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        final PlayerBanData ban = new PlayerBanData(player, actor, reason, expires);
        boolean created = false;

        try {
          created = plugin.getPlayerBanStorage().ban(ban);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!created) {
          return;
        }

        if (plugin.getPlayerStorage().isOnline(player.getUUID())) {
          plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

            @Override
            public void run() {
              Player bukkitPlayer = plugin.getServer().getPlayer(player.getUUID());

              Message kickMessage = Message.get("tempban.player.kick").set("displayName", bukkitPlayer.getDisplayName())
                                           .set("player", player.getName()).set("reason", ban.getReason())
                                           .set("actor", actor.getName())
                                           .set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));

              bukkitPlayer.kickPlayer(kickMessage.toString());
            }

          });
        }

      }

    });

    return true;
  }
}
