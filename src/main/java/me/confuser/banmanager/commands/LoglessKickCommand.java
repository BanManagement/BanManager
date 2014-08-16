package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoglessKickCommand extends BukkitCommand<BanManager> {

	public LoglessKickCommand() {
		super("nlkick");
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String commandName, final String[] args) {
		if (args.length < 1)
			return false;
		
		String playerName = args[0];
		Player player = Bukkit.getPlayer(playerName);
		
		if (player == null) {
			Message message = Message.get("playerOffline")
				.set("[player]", playerName);

			sender.sendMessage(message.toString());
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
			kickMessage = Message.get("kickNoReason");
		} else {
			kickMessage = Message.get("kickReason").set("reason", reason);
		}
		
		kickMessage
			.set("displayName", player.getDisplayName())
			.set("player", player.getName())
			.set("actor", actor.getName());
			
		player.kickPlayer(kickMessage.toString());

		return true;
	}

}
