package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoglessKickCommand extends BukkitCommand<BanManager> {

  public LoglessKickCommand() {
    super("nlkick");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, final String[] args) {
    if (args.length < 1) {
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

    String playerName = args[0];
    Player player = plugin.getServer().getPlayer(playerName);

    if (player == null) {
      Message message = Message.get("sender.error.offline")
                               .set("[player]", playerName);

      sender.sendMessage(message.toString());
      return true;
    } else if (!sender.hasPermission("bm.exempt.override.kick") && player.hasPermission("bm.exempt.kick")) {
      Message.get("sender.error.exempt").set("player", player.getName()).sendTo(sender);
      return true;
    }

    String reason = args.length > 1 ? StringUtils.join(args, " ", 1, args.length - 1) : "";

    PlayerData actor;

    if (sender instanceof Player) {
      actor = plugin.getPlayerStorage().getOnline((Player) sender);
    } else {
      actor = plugin.getPlayerStorage().getConsole();
    }

    Message kickMessage;

    if (reason.isEmpty()) {
      kickMessage = Message.get("kick.player.noReason");
    } else {
      kickMessage = Message.get("kick.player.reason").set("reason", reason);
    }

    kickMessage
            .set("displayName", player.getDisplayName())
            .set("player", player.getName())
            .set("actor", actor.getName());

    player.kickPlayer(kickMessage.toString());

    Message message = Message.get("kick.notify");
    message.set("player", player.getName()).set("actor", actor.getName()).set("reason", reason);

    if (!sender.hasPermission("bm.notify.kick")) {
      message.sendTo(sender);
    }

    plugin.getServer().broadcast(message.toString(), "bm.notify.kick");

    return true;
  }

}
