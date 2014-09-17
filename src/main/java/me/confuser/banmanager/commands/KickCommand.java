package me.confuser.banmanager.commands;

import java.sql.SQLException;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerKickData;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand extends BukkitCommand<BanManager> {

	public KickCommand() {
		super("kick");
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String commandName, final String[] args) {
		if (args.length < 1)
			return false;
		
		if (args[0].toLowerCase().equals(sender.getName().toLowerCase())) {
			sender.sendMessage(Message.getString("noSelf"));
			return true;
		}
		
		final String playerName = args[0];
		Player player = Bukkit.getPlayer(playerName);
		
		if (player == null) {
			Message message = Message.get("playerOffline")
				.set("[player]", playerName);

			sender.sendMessage(message.toString());
			return true;
		}
		
		final String reason = args.length > 1 ? StringUtils.join(args, " ", 1, args.length) : "";
		
		final PlayerData actor;
		
		if (sender instanceof Player) {
			actor = plugin.getPlayerStorage().getOnline((Player) sender);
		} else {
			actor = plugin.getPlayerStorage().getConsole();
		}
		
		if (plugin.getDefaultConfig().isKickLoggingEnabled()) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

				@Override
				public void run() {
					PlayerData player = plugin.getPlayerStorage().retrieve(playerName, false);
					
					PlayerKickData data = new PlayerKickData(player, actor, reason);
					
					boolean created = false;
					
					try {
						created = plugin.getPlayerKickStorage().addKick(data);
					} catch (SQLException e) {
						sender.sendMessage(Message.get("errorOccurred").toString());
						e.printStackTrace();
						return;
					}
					
					if (!created)
						return;
				}
				
			});
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
		
		Message message = Message.get("playerKicked");
		message.set("player", player.getName()).set("actor", actor.getName()).set("reason", reason);

		plugin.getServer().broadcast(message.toString(), "bm.notify.kick");

		return true;
	}

}
