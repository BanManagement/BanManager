package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class BanCommand extends AutoCompleteNameTabCommand<BanManager> {

  public BanCommand() {
    super("ban");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 2) {
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
    boolean isBanned;

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
      if (!sender.hasPermission("bm.command.ban.offline")) {
        sender.sendMessage(Message.getString("sender.error.offlinePermission"));
        return true;
      }
    } else if (!sender.hasPermission("bm.exempt.override.ban") && onlinePlayer.hasPermission("bm.exempt.ban")) {
      Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
      return true;
    }

    final String reason = StringUtils.join(args, " ", 1, args.length);

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

        if (plugin.getExemptionsConfig().isExempt(player, "ban")) {
          sender.sendMessage(Message.get("sender.error.exempt").set("player", playerName).toString());
          return;
        }

        final PlayerData actor;

        if (sender instanceof Player) {
          actor = plugin.getPlayerStorage().getOnline((Player) sender);
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        final PlayerBanData ban = new PlayerBanData(player, actor, reason);
        boolean created;

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

              Message kickMessage = Message.get("ban.player.kick")
                                           .set("displayName", bukkitPlayer.getDisplayName())
                                           .set("player", player.getName())
                                           .set("reason", ban.getReason())
                                           .set("actor", actor.getName());

              bukkitPlayer.kickPlayer(kickMessage.toString());
            }
          });
        }

      }

    });

    return true;
  }


}
