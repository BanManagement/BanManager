package me.confuserr.banmanager;

import me.confuserr.banmanager.data.BanData;
import me.confuserr.banmanager.data.MuteData;

public class BmAPI {

	private static BanManager plugin = BanManager.getPlugin();

	// Bans
	public static void ban(String player, String banned_by, String reason) {
		plugin.dbLogger.logBan(player, banned_by, reason);

		if (plugin.bukkitBan) {
			plugin.getServer().getOfflinePlayer(player).setBanned(true);
		}
	}

	public static void tempban(String player, String banned_by, String reason, long expires) {
		plugin.dbLogger.logTempBan(player, banned_by, reason, expires);

		if (plugin.bukkitBan) {
			plugin.getServer().getOfflinePlayer(player).setBanned(true);
		}
	}

	public static void unban(String player, String by) {
		plugin.dbLogger.banRemove(player, by);

		if (plugin.bukkitBan) {
			plugin.getServer().getOfflinePlayer(player).setBanned(false);
		}
	}

	public static boolean isBanned(String player) {
		return !plugin.dbLogger.isBanned(player).isEmpty();
	}

	public static BanData getCurrentBan(String player) {
		return plugin.dbLogger.getCurrentBan(player);
	}

	// Mutes
	public static void mute(String player, String muted_by, String reason) {
		if (!plugin.dbLogger.isMuted(player)) {
			plugin.addMute(player, reason, muted_by, (long) 0);
			plugin.dbLogger.logMute(player, muted_by, reason);
		}
	}

	public static void tempmute(String player, String reason, String muted_by, long expires) {
		if (!plugin.dbLogger.isMuted(player)) {
			plugin.addMute(player, reason, muted_by, expires);
			plugin.dbLogger.logMute(player, muted_by, reason);
		}
	}

	public static void unmute(String player, String by) {
		if(plugin.dbLogger.isMuted(player))
			plugin.removeMute(player, player);
	}

	public static boolean isMuted(String player) {
		return plugin.dbLogger.isMuted(player);
	}

	public static MuteData getCurrentMute(String player) {
		return plugin.dbLogger.getCurrentMute(player);
	}
}
