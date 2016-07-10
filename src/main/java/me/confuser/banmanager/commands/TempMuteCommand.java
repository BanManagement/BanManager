package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.parsers.Reason;
import me.confuser.bukkitutil.Message;
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
    CommandParser parser = new CommandParser(args, 2);
    args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(command.getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    final boolean isSoft = parser.isSoft();

    if (isSoft && !sender.hasPermission(command.getPermission() + ".soft")) {
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
    final boolean isMuted;

    if (isUUID) {
      try {
        isMuted = plugin.getPlayerMuteStorage().isMuted(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return true;
      }
    } else {
      isMuted = plugin.getPlayerMuteStorage().isMuted(playerName);
    }

    if (isMuted && !sender.hasPermission("bm.command.tempmute.override")) {
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
    final Reason reason = parser.getReason();

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData player = CommandUtils.getPlayer(sender, playerName);

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        if (plugin.getExemptionsConfig().isExempt(player, "tempmute")) {
          sender.sendMessage(Message.get("sender.error.exempt").set("player", playerName).toString());
          return;
        }

        final PlayerData actor = CommandUtils.getActor(sender);

        if (actor == null) return;

        if (isMuted) {
          PlayerMuteData mute;

          if (isUUID) {
            mute = plugin.getPlayerMuteStorage().getMute(UUID.fromString(playerName));
          } else {
            mute = plugin.getPlayerMuteStorage().getMute(playerName);
          }

          if (mute != null) {
            try {
              plugin.getPlayerMuteStorage().unmute(mute, actor);
            } catch (SQLException e) {
              sender.sendMessage(Message.get("sender.error.exception").toString());
              e.printStackTrace();
              return;
            }
          }
        }

        PlayerMuteData mute = new PlayerMuteData(player, actor, reason.getMessage(), isSoft, expires);
        boolean created;

        try {
          created = plugin.getPlayerMuteStorage().mute(mute, isSilent);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!created) {
          return;
        }

        CommandUtils.handlePrivateNotes(player, actor, reason);

        Player bukkitPlayer = plugin.getServer().getPlayer(player.getUUID());

        if (isSoft || bukkitPlayer == null) return;

        Message muteMessage = Message.get("tempmute.player.disallowed")
                                     .set("displayName", bukkitPlayer.getDisplayName())
                                     .set("player", player.getName())
                                     .set("playerId", player.getUUID().toString())
                                     .set("reason", mute.getReason())
                                     .set("actor", actor.getName())
                                     .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));

        bukkitPlayer.sendMessage(muteMessage.toString());

      }

    });

    return true;
  }

}
