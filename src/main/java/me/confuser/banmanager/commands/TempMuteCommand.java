package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
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

public class TempMuteCommand extends AutoCompleteNameTabCommand<BanManager> {

  public TempMuteCommand() {
    super("tempmute");
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
    boolean isMuted = false;

    if (isUUID) {
      isMuted = plugin.getPlayerMuteStorage().isMuted(UUID.fromString(playerName));
    } else {
      isMuted = plugin.getPlayerMuteStorage().isMuted(playerName);
    }

    if (isMuted) {
      Message message = Message.get("mute.error.exists");
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
      expiresCheck = DateUtils.parseDateDiff(args[1], true);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    if (plugin.getConfiguration().getTimeLimits().isPastLimit(sender, TimeLimitType.PLAYER_MUTE, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;
    final String reason = CommandUtils.getReason(2, args);

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        PlayerData player;

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
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString
                  ());
          return;
        }

        if (plugin.getExemptionsConfig().isExempt(player, "tempban")) {
          sender.sendMessage(Message.get("sender.error.exempt").set("player", playerName).toString());
          return;
        }

        PlayerData actor;

        if (sender instanceof Player) {
          actor = plugin.getPlayerStorage().getOnline((Player) sender);
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        PlayerMuteData mute = new PlayerMuteData(player, actor, reason, expires);
        boolean created;

        try {
          created = plugin.getPlayerMuteStorage().mute(mute);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!created) {
          return;
        }

        if (plugin.getPlayerStorage().isOnline(player.getUUID())) {
          Player bukkitPlayer = plugin.getServer().getPlayer(player.getUUID());

          Message muteMessage = Message.get("tempmute.player.disallowed")
                                       .set("displayName", bukkitPlayer.getDisplayName())
                                       .set("player", player.getName())
                                       .set("reason", mute.getReason())
                                       .set("actor", actor.getName())
                                       .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));

          bukkitPlayer.sendMessage(muteMessage.toString());
        }

      }

    });

    return true;
  }

}
