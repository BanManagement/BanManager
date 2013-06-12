package me.confuserr.banmanager;

import me.confuserr.banmanager.data.BanData;
import me.confuserr.banmanager.data.MuteData;

public class BmAPI {

	private static BanManager plugin = BanManager.getPlugin();

	// Bans
	/**
	 * Permanently ban a player
	 * 
	 * @param name - Name of player to ban
	 * @param bannedBy - Who the ban is by, can be anything
	 * @param reason - Why they are banned
	 */
	public static void ban(String name, String bannedBy, String reason) {
		plugin.addPlayerBan(name, reason, bannedBy);

		if (plugin.useBukkitBans()) {
			plugin.getServer().getOfflinePlayer(player).setBanned(true);
		}
	}

	/**
	 * Temporarily ban a player
	 * 
	 * @param name - Name of player to ban
	 * @param bannedBy - Who the ban is by, can be anything
	 * @param reason - Why they are banned
	 * @param expires - Unix Timestamp stating the time of when the ban ends
	 */
	public static void tempban(String name, String bannedBy, String reason, long expires) {
		plugin.dbLogger.logTempBan(player, bannedBy, reason, expires);

		if (plugin.useBukkitBans()) {
			plugin.getServer().getOfflinePlayer(player).setBanned(true);
		}
	}

	/**
	 * Unban a player
	 * 
	 * @param name - Name of player to unban
	 * @param by - Who the unban is by, can be anything
	 */
	public static void unban(String name, String by) {
		plugin.dbLogger.banRemove(player, by);

		if (plugin.useBukkitBans()) {
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
	 * @param name - Name of player
	 * @return - BanData object, if the player is not banned, it returns null
	 */
	public static BanData getCurrentBan(String name) {
		return plugin.dbLogger.getCurrentBan(player);
	}

	// Mutes
	/**
	 * Permanently mute a player
	 * 
	 * @param name - Name of player to mute
	 * @param mutedBy - Who the mute is by, can be anything
	 * @param reason - Why they are muted
	 */
	public static void mute(String name, String mutedBy, String reason) {
		if (!plugin.dbLogger.isMuted(player)) {
			plugin.addMute(player, reason, mutedBy, (long) 0);
			plugin.dbLogger.logMute(player, mutedBy, reason);
		}
	}

	/**
	 * Temporarily mute a player
	 * 
	 * @param name - Name of player to mute
	 * @param mutedBy - Who the mute is by, can be anything
	 * @param reason - Why they are muted
	 * @param expires - Unix Timestamp stating the time of when the ban ends
	 */
	public static void tempmute(String name, String mutedBy, String reason, long expires) {
		if (!plugin.dbLogger.isMuted(name)) {
			plugin.addMute(player, reason, mutedBy, expires);
			plugin.dbLogger.logMute(player, mutedBy, reason);
		}
	}

	/**
	 * Unmute a player
	 * 
	 * @param name - Name of player to unmute
	 * @param by - Who the unmute is by, can be anything
	 */
	public static void unmute(String name, String by) {
		if (plugin.dbLogger.isMuted(player))
			plugin.removeMute(player, player);
	}

	/**
	 * Check if a player is muted
	 * 
	 * @param name - Name of player to check
	 * @return true if the player is currently muted, whether they are permanently muted or temporarily muted
	 */
	public static boolean isMuted(String name) {
		return plugin.dbLogger.isMuted(player);
	}

	/**
	 * Get mute information of a player
	 * 
	 * @param name - Name of player
	 * @return - MuteData object, if the player is not banned, it returns null
	 */
	public static MuteData getCurrentMute(String name) {
		return plugin.dbLogger.getCurrentMute(player);
	}
}
