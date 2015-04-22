package me.confuser.banmanager.commands.external;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.external.ExternalPlayerBanRecordData;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class UnbanAllCommand extends BukkitCommand<BanManager> {

  public UnbanAllCommand() {
    super("unbanall");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 1) {
      return false;
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

    if (!isBanned) {
      Message message = Message.get("notBanned");
      message.set("player", playerName);

      sender.sendMessage(message.toString());
      return true;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        PlayerBanData ban;

        if (isUUID) {
          ban = plugin.getPlayerBanStorage().getBan(UUID.fromString(playerName));
        } else {
          ban = plugin.getPlayerBanStorage().getBan(playerName);
        }

        if (ban == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        PlayerData actor;

        if (sender instanceof Player) {
          actor = plugin.getPlayerStorage().getOnline((Player) sender);
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        ExternalPlayerBanRecordData record = new ExternalPlayerBanRecordData(ban.getPlayer(), actor);

        int unbanned;

        try {
          unbanned = plugin.getExternalPlayerBanRecordStorage().create(record);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("errorOccurred").toString());
          e.printStackTrace();
          return;
        }

        if (unbanned == 0) {
          return;
        }

        Message.get("unbanall.notify")
               .set("actor", actor.getName())
               .set("player", ban.getPlayer().getName())
               .sendTo(sender);
      }

    });

    return true;
  }
}
