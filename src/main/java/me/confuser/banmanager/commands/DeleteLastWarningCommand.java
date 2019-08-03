package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.common.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class DeleteLastWarningCommand extends AutoCompleteNameTabCommand<BanManager> {

  public DeleteLastWarningCommand() {
    super("dwarn");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 1) {
      return false;
    }

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, commandName, args);
      return true;
    }

    // Check if UUID vs name
    final String playerName = args[0];
    final boolean isUUID = playerName.length() > 16;

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData player;

        if (isUUID) {
          try {
            player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(UUID.fromString(playerName)));
          } catch (Exception e) {
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

        int updated = 0;
        try {
          updated = plugin.getPlayerWarnStorage().deleteRecent(player);
        } catch (SQLException e) {
          e.printStackTrace();
        }

        if (updated == 0) {
          Message.get("dwarn.error.noWarnings").set("player", player.getName()).sendTo(sender);
        } else {
          Message.get("dwarn.notify").set("player", player.getName()).set("actor", sender.getName()).sendTo(sender);

          Player bukkitPlayer = CommandUtils.getPlayer(player.getUUID());

          if (bukkitPlayer == null) return;

          Message.get("dwarn.player.notify")
                 .set("player", player.getName())
                 .set("playerId", player.getUUID().toString())
                 .set("actor", sender.getName())
                 .sendTo(bukkitPlayer);
        }
      }

    });

    return true;
  }
}
