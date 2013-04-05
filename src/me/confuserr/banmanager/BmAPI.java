package me.confuserr.banmanager;

import me.confuserr.banmanager.data.BanData;
import me.confuserr.banmanager.data.MuteData;

public class BmAPI {

	private static BanManager plugin = BanManager.getPlugin();

	// Bans
	/**
	 * Permanently ban a player
	 * 
	 * @param player - Name of player to ban
	 * @param banned_by - Who the ban is by, can be anything
	 * @param reason - Why they are banned
	 */
	public static void ban(String player, String banned_by, String reason) {
		plugin.dbLogger.logBan(player, banned_by, reason);

		if (plugin.bukkitBan) {
			plugin.getServer().getOfflinePlayer(player).setBanned(true);
		}
	}

	/**
	 * Temporarily ban a player
	 * 
	 * @param player - Name of player to ban
	 * @param banned_by - Who the ban is by, can be anything
	 * @param reason - Why they are banned
	 * @param expires - Unix Timestamp stating the time of when the ban ends
	 */
	public static void tempban(String player, String banned_by, String reason, long expires) {
		plugin.dbLogger.logTempBan(player, banned_by, reason, expires);

		if (plugin.bukkitBan) {
			plugin.getServer().getOfflinePlayer(player).setBanned(true);
		}
	}

	/**
	 * Unban a player
	 * 
	 * @param player - Name of player to unban
	 * @param by - Who the unban is by, can be anything
	 */
	public static void unban(String player, String by) {
		plugin.dbLogger.banRemove(player, by);

		if (plugin.bukkitBan) {
			plugin.getServer().getOfflinePlayer(player).setBanned(false);
		}
	}

	/**
	 * Check if a player is banned
	 * 
	 * @param player - Name of player to check
	 * @return true if the player is currently banned, whether they are permanently banned or temporarily banned
	 */
	public static boolean isBanned(String player) {
		return !plugin.dbLogger.isBanned(player).isEmpty();
	}

	
	/**
	 * Get ban information of a player
	 * 
	 * @param player - Name of player
	 * @return - BanData object, if the player is not banned, it returns null
	 */
	public static BanData getCurrentBan(String player) {
		return plugin.dbLogger.getCurrentBan(player);
	}

	// Mutes
	/**
	 * Permanently mute a player
	 * 
	 * @param player - Name of player to mute
	 * @param muted_by - Who the mute is by, can be anything
	 * @param reason - Why they are muted
	 */
	public static void mute(String player, String muted_by, String reason) {
		if (!plugin.dbLogger.isMuted(player)) {
			plugin.addMute(player, reason, muted_by, (long) 0);
			plugin.dbLogger.logMute(player, muted_by, reason);
		}
	}

	/**
	 * Temporarily mute a player
	 * 
	 * @param player - Name of player to mute
	 * @param muted_by - Who the mute is by, can be anything
	 * @param reason - Why they are muted
	 * @param expires - Unix Timestamp stating the time of when the ban ends
	 */
	public static void tempmute(String player, String muted_by, String reason, long expires) {
		if (!plugin.dbLogger.isMuted(player)) {
			plugin.addMute(player, reason, muted_by, expires);
			plugin.dbLogger.logMute(player, muted_by, reason);
		}
	}

	/**
	 * Unmute a player
	 * 
	 * @param player - Name of player to unmute
	 * @param by - Who the unmute is by, can be anything
	 */
	public static void unmute(String player, String by) {
		if (plugin.dbLogger.isMuted(player))
			plugin.removeMute(player, player);
	}

	/**
	 * Check if a player is muted
	 * 
	 * @param player - Name of player to check
	 * @return true if the player is currently muted, whether they are permanently muted or temporarily muted
	 */
	public static boolean isMuted(String player) {
		return plugin.dbLogger.isMuted(player);
	}

	/**
	 * Get mute information of a player
	 * 
	 * @param player - Name of player
	 * @return - MuteData object, if the player is not banned, it returns null
	 */
	public static MuteData getCurrentMute(String player) {
		return plugin.dbLogger.getCurrentMute(player);
	}
}
