package me.confuser.banmanager.commands;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.net.InetAddresses;

public class InfoCommand extends BukkitCommand<BanManager> {
	
	public InfoCommand() {
		super("bminfo");
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
		if (args.length < 1)
			return false;
		
		final String search = args[0];
		final boolean isName = !InetAddresses.isInetAddress(search);
		
		if (isName && search.length() > 16) {
			Message message = Message.get("invalidIp");
			message.set("ip", search);

			sender.sendMessage(message.toString());
			return true;
		}
		
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				if (isName) {
					try {
						playerInfo(sender, search);
					} catch (SQLException e) {
						sender.sendMessage(Message.get("errorOccurred").toString());
						e.printStackTrace();
						return;
					}
				}/* else {
					TODO
					ipInfo(sender, search);
				}*/
			}
			
		});
		
		return true;
	}
	
	public void playerInfo(CommandSender sender, String name) throws SQLException {
		PlayerData player = plugin.getPlayerStorage().retrieve(name, false);
		
		if (player == null) {
			sender.sendMessage(Message.get("playerNotFound").set("player", name).toString());
			return;
		}
		
		ArrayList<String> messages = new ArrayList<String>();
		
		long banTotal = plugin.getPlayerBanRecordStorage().getCount(player);
		long muteTotal = plugin.getPlayerMuteRecordStorage().getCount(player);
		long warnTotal = plugin.getPlayerWarnStorage().getCount(player);
		
		messages.add(Message.get("bminfoStatsSummary")
			.set("player", player.getName())
			.set("bans", Long.toString(banTotal))
			.set("mutes", Long.toString(muteTotal))
			.set("warns", Long.toString(warnTotal)).toString());
		
		messages.add(Message.get("bminfoConnection")
			.set("ip", IPUtils.toString(player.getIP()))
			.set("lastSeen", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new java.util.Date(player.getLastSeen() * 1000L)))
			.toString());
		
		messages.add(Message.getString("altsFound"));
		
		StringBuilder duplicates = new StringBuilder();
		
		for (PlayerData duplicatePlayer : plugin.getPlayerStorage().getDuplicates(player.getIP())) {
			duplicates.append(duplicatePlayer.getName() + ", ");
		}
		
		duplicates.setLength(duplicates.length() - 2);
		
		messages.add(duplicates.toString());
		
		
		long ipBanTotal = plugin.getIpBanRecordStorage().getCount(player.getIP());
		
		messages.add(Message.get("bminfoIpStatsSummary")
			.set("bans", Long.toString(ipBanTotal))
			.toString());
		
		if (plugin.getPlayerBanStorage().isBanned(player.getUUID())) {
			PlayerBanData ban = plugin.getPlayerBanStorage().getBan(player.getUUID());
			
			messages.add(Message.get("bminfoBanned")
				.set("player", player.getName())
				.set("reason", ban.getReason())
				.set("actor", ban.getActor().getName())
				.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()))
				.set("created", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new java.util.Date(ban.getCreated() * 1000L)))
				.toString());
		}

		if (plugin.getPlayerMuteStorage().isMuted(player.getUUID())) {
			PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(player.getUUID());
			
			messages.add(Message.get("bminfoMuted")
				.set("player", player.getName())
				.set("reason", mute.getReason())
				.set("actor", mute.getActor().getName())
				.set("expires", DateUtils.getDifferenceFormat(mute.getExpires()))
				.set("created", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new java.util.Date(mute.getCreated() * 1000L)))
				.toString());
		}
		
		// TODO Show last warning
		
		for (String message : messages) {
			sender.sendMessage(message);
		}
	}
}
