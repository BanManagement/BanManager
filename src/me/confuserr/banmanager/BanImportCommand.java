package me.confuserr.banmanager;

import java.util.Iterator;
import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanImportCommand implements CommandExecutor {

	private BanManager plugin;

	BanImportCommand(BanManager instance) {
		plugin = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		if(args.length < 2)
			return false;
		else if(!args[0].equals("player") && !args[0].equals("ip"))
			return false;
		else if(plugin.importInProgress) {
			plugin.sendMessage(sender, plugin.banMessages.get("importInProgressError"));
			return true;
		}
		
		Player player = null;
		String playerName = "Console";
		
		if(sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if(!player.hasPermission("bm.import")) {
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		
		String type = args[0];
		String reason = args[1];
		
		plugin.importInProgress = true;
		
		if(type.equals("player")) {
			plugin.sendMessage(sender, plugin.banMessages.get("beginingPlayerImport"));
			
			Set<OfflinePlayer> banned = plugin.getServer().getBannedPlayers();
			
			plugin.sendMessage(sender, plugin.banMessages.get("scanningDatabase"));
			
			Iterator<OfflinePlayer> it = banned.iterator();
			
			while(it.hasNext()) {
				OfflinePlayer next = it.next();
				if( plugin.dbLogger.playerInTable(next.getName()) )
					it.remove();
			}
			
			plugin.sendMessage(sender, plugin.banMessages.get("scanPlayersFound").replace("[found]", Integer.toString(banned.size())));
			
			if(banned.size() == 0)
				plugin.sendMessage(sender, plugin.banMessages.get("noPlayersImport"));
			else {
				int done = 0;
				double percent = 0;
				int totalPlayers = banned.size();
				
				for(OfflinePlayer p : banned) {
					plugin.dbLogger.logBan(p.getName(), playerName, reason);
					done++;
					percent = Math.round((double) (done * 100) / totalPlayers);
					if(percent % 10 == 0 && (int) percent != 100)
						plugin.sendMessage(sender, plugin.banMessages.get("percentagePlayersImported").replace("[percent]", Double.toString(percent)));
				}
				
				banned.clear();
				
				plugin.sendMessage(sender, plugin.banMessages.get("playerImportComplete"));
			}
		} else if(type.equals("ip")) {
			plugin.sendMessage(sender, plugin.banMessages.get("beginingIpImport"));
			
			Set<String> banned = plugin.getServer().getIPBans();
			
			plugin.sendMessage(sender, plugin.banMessages.get("scanningDatabase"));
			
			Iterator<String> it = banned.iterator();
			
			while(it.hasNext()) {
				String next = it.next();
				if(plugin.dbLogger.ipInTable(next))
					it.remove();
			}
			
			plugin.sendMessage(sender, plugin.banMessages.get("scanIpsFound").replace("[found]", Integer.toString(banned.size())));
			
			if(banned.size() == 0)
				plugin.sendMessage(sender, plugin.banMessages.get("noIpsImport"));
			else {
				int done = 0;
				double percent = 0;
				int totalPlayers = banned.size();
				
				for(String p : banned) {
					plugin.dbLogger.logIpBan(p, playerName, reason);
					done++;
					percent = Math.round((double) (done * 100) / totalPlayers);
					if(percent % 10 == 0 && (int) percent != 100)
						plugin.sendMessage(sender, plugin.banMessages.get("percentageIpsImported").replace("[percent]", Double.toString(percent)));
				}
				
				banned.clear();
				
				plugin.sendMessage(sender, plugin.banMessages.get("ipImportComplete"));
			}
		}
		plugin.importInProgress = false;
		
		return true;
	}

}
