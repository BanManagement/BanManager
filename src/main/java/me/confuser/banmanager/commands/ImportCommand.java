package me.confuser.banmanager.commands;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonReader;

import com.google.common.net.InetAddresses;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;

public class ImportCommand extends BukkitCommand<BanManager> {
	private boolean importInProgress = false;
	
	public ImportCommand() {
		super("bmimport");
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String commandName, final String[] args) {
		if (args.length != 1)
			return false;
		
		if (!args[0].equals("player") && !args[0].equals("ip") && !args[0].equals("players") && !args[0].equals("ips"))
			return false;
		if (importInProgress) {
			sender.sendMessage(Message.get("importInProgressError").toString());
			return true;
		}
				
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				if (args[0].startsWith("player")) {
					importPlayers();
				} else if (args[0].startsWith("ip")) {
					importIps();
				}
			}
			
		});
		
		return true;
	}
	
	private void importPlayers() {
		importInProgress = true;

		try {
			JsonReader reader = new JsonReader(new FileReader("banned-players.json"));
			reader.beginArray();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			
			while(reader.hasNext()) {
				reader.beginObject();
				
				UUID uuid = null;
				String name = null;
				Long created = null;
				PlayerData actor = null;
				Long expires = null;
				String reason = null;
				
				while(reader.hasNext()) {
					switch (reader.nextName()) {
						case "uuid":
							uuid = UUID.fromString(reader.nextString());
						break;
						case "name":
							name = reader.nextString();
						break;
						case "created":
						try {
							created = dateFormat.parse(reader.nextString()).getTime() / 1000L;
						} catch (ParseException e) {
							e.printStackTrace();
							
							continue;
						}
						break;
						case "source":
							String sourceName = reader.nextString();
							
							if (sourceName.equals("CONSOLE")) {
								actor = plugin.getPlayerStorage().getConsole();
							} else {
								actor = plugin.getPlayerStorage().retrieve(sourceName, false);
								
								if (actor == null) {
									actor = plugin.getPlayerStorage().getConsole();
								}
							}
						break;
						case "expires":
							String expiresStr = reader.nextString();
							
							if (expiresStr.equals("forever")) {
								expires = 0L;
							} else {
								try {
									created = dateFormat.parse(reader.nextString()).getTime() / 1000L;
								} catch (ParseException e) {
									e.printStackTrace();
									
									continue;
								}
							}
						break;
						case "reason":
							reason = reader.nextString();
						break;
					}
				}
				
				reader.endObject();
				
				if (uuid == null || name == null || created == null || actor == null || expires == null || reason == null) {
					continue;
				}
				
				if (plugin.getPlayerBanStorage().isBanned(uuid)) {
					continue;
				}
				
				PlayerData player = plugin.getPlayerStorage().retrieve(name, true);
				
				if (player == null) {
					plugin.getLogger().warning("Unable to import " + name + " due to look up issue");
					continue;
				}
				
				PlayerBanData ban = new PlayerBanData(player, actor, reason, expires, created);
				try {
					plugin.getPlayerBanStorage().create(ban);
				} catch (SQLException e) {
					e.printStackTrace();
					continue;
				}
			}
			
			reader.endArray();
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		importInProgress = false;
	}
	
	private void importIps() {
		importInProgress = true;

		try {
			JsonReader reader = new JsonReader(new FileReader("banned-ips.json"));
			reader.beginArray();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			
			while(reader.hasNext()) {
				reader.beginObject();
				
				String ipStr = null;
				Long created = null;
				PlayerData actor = null;
				Long expires = null;
				String reason = null;
				
				while(reader.hasNext()) {
					switch (reader.nextName()) {
						case "ip":
							ipStr = reader.nextString();
						break;
						case "created":
						try {
							created = dateFormat.parse(reader.nextString()).getTime() / 1000L;
						} catch (ParseException e) {
							e.printStackTrace();
							
							continue;
						}
						break;
						case "source":
							String sourceName = reader.nextString();
							
							if (sourceName.equals("CONSOLE")) {
								actor = plugin.getPlayerStorage().getConsole();
							} else {
								actor = plugin.getPlayerStorage().retrieve(sourceName, false);
								
								if (actor == null) {
									actor = plugin.getPlayerStorage().getConsole();
								}
							}
						break;
						case "expires":
							String expiresStr = reader.nextString();
							
							if (expiresStr.equals("forever")) {
								expires = 0L;
							} else {
								try {
									created = dateFormat.parse(reader.nextString()).getTime() / 1000L;
								} catch (ParseException e) {
									e.printStackTrace();
									
									continue;
								}
							}
						break;
						case "reason":
							reason = reader.nextString();
						break;
					}
				}
				
				reader.endObject();
				
				if (ipStr == null || created == null || actor == null || expires == null || reason == null) {
					continue;
				}
				
				if (!InetAddresses.isInetAddress(ipStr)) {
					continue;
				}
				
				long ip = IPUtils.toLong(ipStr);
				
				if (plugin.getIpBanStorage().isBanned(ip)) {
					continue;
				}
				
				IpBanData ban = new IpBanData(ip, actor, reason, expires, created);
				try {
					plugin.getIpBanStorage().create(ban);
				} catch (SQLException e) {
					e.printStackTrace();
					continue;
				}
			}
			
			reader.endArray();
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		importInProgress = false;
	}
}
