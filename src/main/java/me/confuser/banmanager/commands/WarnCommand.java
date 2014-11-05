package me.confuser.banmanager.commands;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerWarnData;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;

public class WarnCommand extends BukkitCommand<BanManager> {

	public WarnCommand() {
		super("warn");
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
		if (args.length < 2) {
			return false;
		}

		if (args[0].toLowerCase().equals(sender.getName().toLowerCase())) {
			sender.sendMessage(Message.getString("noSelf"));
			return true;
		}

		// Check if UUID vs name
		final String playerName = args[0];
		final boolean isUUID = playerName.length() > 16;
		final String reason = StringUtils.join(args, " ", 1, args.length);

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				PlayerData player;

				if (isUUID) {
					try {
						player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(UUID.fromString(playerName)));
					} catch (SQLException e) {
						sender.sendMessage(Message.get("errorOccurred").toString());
						e.printStackTrace();
						return;
					}
				} else {
					player = plugin.getPlayerStorage().retrieve(playerName, true);
				}

				if (player == null) {
					sender.sendMessage(Message.get("playerNotFound").set("player", playerName).toString());
					return;
				}

				try {
					if (plugin.getPlayerWarnStorage().isRecentlyWarned(player)) {
						Message.get("warnCooldown").sendTo(sender);
						return;
					}
				} catch (SQLException e) {
					sender.sendMessage(Message.get("errorOccurred").toString());
					e.printStackTrace();
					return;
				}

				PlayerData actor;

				if (sender instanceof Player) {
					actor = plugin.getPlayerStorage().getOnline((Player) sender);
				} else {
					actor = plugin.getPlayerStorage().getConsole();
				}

				boolean isOnline = plugin.getPlayerStorage().isOnline(player.getUUID());

				PlayerWarnData warning = new PlayerWarnData(player, actor, reason, isOnline);

				boolean created = false;

				try {
					created = plugin.getPlayerWarnStorage().addWarning(warning);
				} catch (SQLException e) {
					sender.sendMessage(Message.get("errorOccurred").toString());
					e.printStackTrace();
					return;
				}

				if (!created) {
					return;
				}

				if (isOnline) {
					Player bukkitPlayer = plugin.getServer().getPlayer(player.getUUID());

					Message warningMessage = Message.get("warned")
						.set("displayName", bukkitPlayer.getDisplayName())
						.set("player", player.getName())
						.set("reason", warning.getReason())
						.set("actor", actor.getName());

					bukkitPlayer.sendMessage(warningMessage.toString());
				}

				Message message = Message.get("playerWarned")
					.set("player", player.getName())
					.set("actor", actor.getName())
					.set("reason", warning.getReason());

				if (!sender.hasPermission("bm.notify.warn")) {
					message.sendTo(sender);
				}

				plugin.getServer().broadcast(message.toString(), "bm.notify.warn");
			}

		});

		return true;
	}
}
