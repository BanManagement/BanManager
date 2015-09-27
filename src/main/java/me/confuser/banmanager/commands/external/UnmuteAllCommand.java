package me.confuser.banmanager.commands.external;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.data.external.ExternalPlayerMuteRecordData;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class UnmuteAllCommand extends BukkitCommand<BanManager> {

  public UnmuteAllCommand() {
    super("unmuteall");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 1) {
      return false;
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

    if (!isMuted) {
      Message message = Message.get("unmute.error.noExists");
      message.set("player", playerName);

      sender.sendMessage(message.toString());
      return true;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        PlayerMuteData mute;

        if (isUUID) {
          mute = plugin.getPlayerMuteStorage().getMute(UUID.fromString(playerName));
        } else {
          mute = plugin.getPlayerMuteStorage().getMute(playerName);
        }

        if (mute == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        PlayerData actor;

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

        ExternalPlayerMuteRecordData record = new ExternalPlayerMuteRecordData(mute.getPlayer(), actor);

        int unmuted;

        try {
          unmuted = plugin.getExternalPlayerMuteRecordStorage().create(record);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("errorOccurred").toString());
          e.printStackTrace();
          return;
        }

        if (unmuted == 0) {
          return;
        }

        Message.get("unmuteall.notify")
               .set("actor", actor.getName())
               .set("player", mute.getPlayer().getName())
               .sendTo(sender);
      }

    });

    return true;
  }
}
