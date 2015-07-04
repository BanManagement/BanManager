package me.confuser.banmanager.commands.external;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.external.ExternalPlayerMuteData;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by James on 26/01/2015.
 */
public class TempMuteAllCommand extends BukkitCommand<BanManager> {

  public TempMuteAllCommand() {
    super("tempmuteall");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 3) {
      return false;
    }

    if (args[0].toLowerCase().equals(sender.getName().toLowerCase())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    // Check if UUID vs name
    final String playerName = args[0];
    final boolean isUUID = playerName.length() > 16;

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(args[1], true);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("invalidTime").toString());
      return true;
    }

    if (plugin.getConfiguration().getTimeLimits().isPastLimit(sender, TimeLimitType.PLAYER_MUTE, expiresCheck)) {
      Message.get("timeLimitError").sendTo(sender);
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
          try {
            actor = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes((Player) sender));
          } catch (SQLException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        final ExternalPlayerMuteData mute = new ExternalPlayerMuteData(player, actor, reason, expires);
        int created;

        try {
          created = plugin.getExternalPlayerMuteStorage().create(mute);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (created != 1) {
          return;
        }

        Message.get("tempmuteall.notify")
               .set("actor", mute.getActorName())
               .set("reason", mute.getReason())
               .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()))
               .set("player", player.getName())
               .sendTo(sender);
      }

    });

    return true;
  }

}
