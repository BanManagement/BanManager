package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerWarnData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class WarnCommand extends AutoCompleteNameTabCommand<BanManager> {

  public WarnCommand() {
    super("warn");
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
    final String reason = StringUtils.join(args, " ", 1, args.length);

    Player onlinePlayer;

    if (isUUID) {
      onlinePlayer = plugin.getServer().getPlayer(UUID.fromString(playerName));
    } else {
      onlinePlayer = plugin.getServer().getPlayer(playerName);
    }

    if (onlinePlayer == null) {
      if (!sender.hasPermission("bm.command.warn.offline")) {
        sender.sendMessage(Message.getString("sender.error.offlinePermission"));
        return true;
      }
    } else if (!sender.hasPermission("bm.exempt.override.warn") && onlinePlayer.hasPermission("bm.exempt.warn")) {
      Message.get("sender.error.exempt").set("player", onlinePlayer.getName()).sendTo(sender);
      return true;
    }

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

        if (plugin.getExemptionsConfig().isExempt(player, "tempban")) {
          sender.sendMessage(Message.get("sender.error.exempt").set("player", playerName).toString());
          return;
        }

        try {
          if (plugin.getPlayerWarnStorage().isRecentlyWarned(player)) {
            Message.get("warn.error.cooldown").sendTo(sender);
            return;
          }
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        final PlayerData actor;

        if (sender instanceof Player) {
          actor = plugin.getPlayerStorage().getOnline((Player) sender);
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        boolean isOnline = plugin.getPlayerStorage().isOnline(player.getUUID());

        final PlayerWarnData warning = new PlayerWarnData(player, actor, reason, isOnline);

        boolean created = false;

        try {
          created = plugin.getPlayerWarnStorage().addWarning(warning);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!created) {
          return;
        }

        if (isOnline) {
          Player bukkitPlayer = plugin.getServer().getPlayer(player.getUUID());

          Message warningMessage = Message.get("warn.player.warned")
                                          .set("displayName", bukkitPlayer.getDisplayName())
                                          .set("player", player.getName())
                                          .set("reason", warning.getReason())
                                          .set("actor", actor.getName());

          bukkitPlayer.sendMessage(warningMessage.toString());
        }

        Message message = Message.get("warn.notify")
                                 .set("player", player.getName())
                                 .set("actor", actor.getName())
                                 .set("reason", warning.getReason());

        if (!sender.hasPermission("bm.notify.warn")) {
          message.sendTo(sender);
        }

        plugin.getServer().broadcast(message.toString(), "bm.notify.warn");

        final List<String> actionCommands;

        try {
          actionCommands = plugin.getConfiguration().getWarningActions()
                                 .getCommand((int) plugin.getPlayerWarnStorage().getCount(player));
        } catch (SQLException e) {
          e.printStackTrace();
          return;
        }

        if (actionCommands == null || actionCommands.isEmpty()) {
          return;
        }

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            for (String command : actionCommands) {
              String actionCommand = command
                      .replace("[player]", player.getName())
                      .replace("[actor]", actor.getName())
                      .replace("[reason]", warning.getReason());

              plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), actionCommand);
            }
          }

        });
      }

    });

    return true;
  }
}
