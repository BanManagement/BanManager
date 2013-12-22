package me.confuser.banmanager;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.confuser.banmanager.data.*;

/**
 * This is a static API for BanManager.
 * All methods are thread safe unless stated otherwise.
 * 
 * Note: The API does not handle the notifications to players
 * nor permission checks for exemptions.
 * 
 * @author James Mortemore
 */
public class BmAPI {

	private static BanManager plugin = BanManager.getPlugin();

	// Bans
	/**
	 * Permanently ban a player.
	 * You must handle kicking the player if they are online.
	 * 
	 * @param name - Name of player to ban
	 * @param bannedBy - Who the ban is by, can be anything
	 * @param reason - Why they are banned
	 */
	public static void ban(String name, String bannedBy, String reason) {
		plugin.addPlayerBan(name, bannedBy, reason);
	}

	/**
	 * Temporarily ban a player
	 * You must handle kicking the player if they are online.
	 * 
	 * @param name - Name of player to ban
	 * @param bannedBy - Who the ban is by, can be anything
	 * @param reason - Why they are banned
	 * @param expires - Unix Timestamp stating the time of when the ban ends
	 */
	public static void tempBan(String name, String bannedBy, String reason, long expires) {
		plugin.addPlayerBan(name, bannedBy, reason, expires);
	}

	/**
	 * Unban a player
	 * 
	 * @param name - Name of player to unban
	 * @param by - Who the unban is by, can be anything
	 * @param keepLog - Whether to store the ban as a record or not
	 */
	public static void unBan(String name, String by, boolean keepLog) {
		plugin.removePlayerBan(name, by, keepLog);
	}

	/**
	 * Check if a player is banned
	 * 
	 * @param name - Name of player to check
	 * @return true if the player is currently banned, whether they are permanently banned or temporarily banned
	 */
	public static boolean isBanned(String name) {
		return plugin.isPlayerBanned(name);
	}

	
	/**
	 * Get ban information of a player
	 * 
	 * @param name - Name of player
	 * @return - BanData object, if the player is not banned, it returns null
	 */
	public static BanData getCurrentBan(String name) {
		return plugin.getPlayerBan(name);
	}
	
	/**
	 * Retrieve past bans of a player.
	 * This method is not thread safe and should not be called on the main thread.
	 * 
	 * @param name - Name of player
	 * @return ArrayList containing data on all bans
	 */
	public static ArrayList<BanData> getPastBans(String name) {
		return plugin.getPlayerPastBans(name);
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
		plugin.addPlayerMute(name, mutedBy, reason);
	}

	/**
	 * Temporarily mute a player
	 * 
	 * @param name - Name of player to mute
	 * @param mutedBy - Who the mute is by, can be anything
	 * @param reason - Why they are muted
	 * @param expires - Unix Timestamp stating the time of when the ban ends
	 */
	public static void tempMute(String name, String mutedBy, String reason, long expires) {
		plugin.addPlayerMute(name, mutedBy, reason, expires);
	}

	/**
	 * Unmute a player
	 * 
	 * @param name - Name of player to unmute
	 * @param by - Who the unmute is by, can be anything
	 * @param keepLog - Whether to store the mute as a record or not
	 */
	public static void unMute(String name, String by, boolean keepLog) {
		plugin.removePlayerMute(name, by, keepLog);
	}

	/**
	 * Check if a player is muted.
	 * 
	 * Due to the nature of mutes only being in memory if the player is online
	 * this method should not be called within the main thread.
	 * 
	 * @param name - Name of player to check
	 * @return true if the player is currently muted, whether they are permanently muted or temporarily muted
	 */
	public static boolean isMuted(String name) {
		return plugin.isPlayerMuted(name);
	}

	/**
	 * Get mute information of a player.
	 * 
	 * Due to the nature of mutes only being in memory if the player is online
	 * this method should not be called within the main thread.
	 * 
	 * @param name - Name of player
	 * @return - MuteData object, if the player is not banned, it returns null
	 */
	public static MuteData getCurrentMute(String name) {
		return plugin.getPlayerMute(name);
	}
	
	/**
	 * Retrieve past mutes of a player.
	 * This method is not thread safe and should not be called on the main thread.
	 * 
	 * @param name - Name of player
	 * @return ArrayList containing data on all mutes
	 */
	public static ArrayList<MuteData> getPastMutes(String name) {
		return plugin.getPlayerPastMutes(name);
	}
	
	// IP Bans
	/**
	 * Permanently ban an IP
	 * You must handle kicking any players with the ip if they are online.
	 * 
	 * @param ip - IP address to ban
	 * @param bannedBy - Who the ban is by, can be anything
	 * @param reason - Why they are banned
	 */
	public static void banIP(String ip, String bannedBy, String reason) {
		plugin.addIPBan(ip, bannedBy, reason);
	}

	/**
	 * Temporarily ban an IP
	 * You must handle kicking any players with the ip if they are online.
	 * 
	 * @param ip - IP address to ban
	 * @param bannedBy - Who the ban is by, can be anything
	 * @param reason - Why they are banned
	 * @param expires - Unix Timestamp stating the time of when the ban ends
	 */
	public static void tempIPBan(String ip, String bannedBy, String reason, long expires) {
		plugin.addIPBan(ip, bannedBy, reason, expires);
	}

	/**
	 * Unban an IP
	 * 
	 * @param name - IP address to unban
	 * @param by - Who the unban is by, can be anything
	 * @param keepLog - Whether to store the ban as a record or not
	 */
	public static void unBanIP(String name, String by, boolean keepLog) {
		plugin.removeIPBan(name, by, keepLog);
	}

	/**
	 * Check if a player is banned
	 * 
	 * @param ip - IP address to check
	 * @return true if the player is currently banned, whether they are permanently banned or temporarily banned
	 */
	public static boolean isIPBanned(String ip) {
		return plugin.isIPBanned(ip);
	}

	
	/**
	 * Get ban information of an IP ban
	 * 
	 * @param ip - IP address
	 * @return - IPBanData object, if the IP is not banned, it returns null
	 */
	public static IPBanData getCurrentIPBan(String ip) {
		return plugin.getIPBan(ip);
	}
	
	/**
	 * Retrieve past ip bans of an IP.
	 * This method is not thread safe and should not be called on the main thread.
	 * 
	 * @param ip - IP address to check
	 * @return ArrayList containing data on all bans
	 */
	public static ArrayList<IPBanData> getPastIPBans(String ip) {
		return plugin.getIPPastBans(ip);
	}
	
	// Warnings
	/**
	 * Warn a player.
	 * You must handle the notification to the warned player yourself.
	 * 
	 * @param name - Name of player to warn
	 * @param bannedBy - Who the warning is by, can be anything
	 * @param reason - Why they are warned
	 */
	public static void warn(String name, String warnedBy, String reason) {
		plugin.addPlayerWarning(name, warnedBy, reason);
	}
	
	/**
	 * Retrieve past warnings of a player.
	 * This method is not thread safe and should not be called on the main thread.
	 * 
	 * @param name - Name of player
	 * @return ArrayList containing data on all warnings
	 */
	public static ArrayList<WarnData> getWarnings(String name) {
		return plugin.getPlayerWarnings(name);
	}
	
	// Kicks
	/**
	 * Kicks a player and logs it to the database.
	 * You must check the player is online first otherwise a NPE will be thrown.
	 * 
	 * @param name - Name of player to kick
	 * @param bannedBy - Who the kick is by, can be anything
	 * @param reason - Why they are kicked
	 */
	public static synchronized void kick(String name, String kickedBy, String reason) {
		String viewReason = Util.viewReason(reason);
		Player target = Bukkit.getPlayer(name);
		
		String kick = getMessage("kickReason").replace("[name]", target.getDisplayName()).replace("[reason]", viewReason).replace("[by]", kickedBy);
		
		target.kickPlayer(kick);
		
		plugin.dbLogger.logKick(name, kickedBy, reason);
	}
	
	// Misc
	/**
	 * @param message - The message config node
	 * @return String
	 */
	public static String getMessage(String message) {
		return plugin.getMessage(message);
	}
	
	/**
	 * Retrieve the players IP.
	 * This method is not thread safe and should not be called on the main thread.
	 * 
	 * @param name - Name of the player
	 * @return - IP of the player, if not found an empty string is returned.
	 */
	public static String getPlayerIP(String name) {
		return plugin.getPlayerIP(name);
	}
}
