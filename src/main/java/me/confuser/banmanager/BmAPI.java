package me.confuser.banmanager;

import me.confuser.banmanager.data.*;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;

import org.bukkit.entity.Player;

import com.j256.ormlite.dao.CloseableIterator;

import java.sql.SQLException;
import java.util.UUID;

/**
 * This is a static API for BanManager.
 * No methods are thread safe unless stated otherwise.
 * <p/>
 * Note: The API does not handle the notifications to players
 * nor permission checks for exemptions.
 *
 * @author James Mortemore
 */
public class BmAPI {

	private static BanManager plugin = BanManager.getPlugin();
	
	/**
	 * @param uuid
	 * @return PlayerData
	 * @throws SQLException
	 */
	public static PlayerData getPlayer(UUID uuid) throws SQLException {
		return plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
	}
	
	/**
	 * @param player
	 * @return PlayerData
	 * @throws SQLException
	 */
	public static PlayerData getPlayer(Player player) throws SQLException {
		return getPlayer(player.getUniqueId());
	}
	
	/**
	 * @param name
	 * @return PlayerData
	 * @throws SQLException
	 */
	public static PlayerData getPlayer(String name) throws SQLException {
		return plugin.getPlayerStorage().retrieve(name, false);
	}
	
	/**
	 * Get the console for use as an actor
	 * @return
	 */
	public static PlayerData getConsole() {
		return plugin.getPlayerStorage().getConsole();
	}
	
	/**
	 * Convert an ip string of x.x.x.x to long
	 * @param ip
	 * @return
	 */
	public static long ipToStr(String ip) {
		return IPUtils.toLong(ip);
	}

	/**
	 * Permanently ban a player.
	 * You must handle kicking the player if they are online.
	 *
	 * @param ban - PlayerBanData
	 * @throws SQLException 
	 */
	public static boolean ban(PlayerBanData ban) throws SQLException {
		return plugin.getPlayerBanStorage().ban(ban);
	}
	
	/**
	 * Permanently ban a player.
	 * You must handle kicking the player if they are online.
	 *
	 * @param player   - Player to ban
	 * @param actor    - Who the ban is by
	 * @param reason   - Why they are banned
	 * @throws SQLException 
	 */
	public static boolean ban(PlayerData player, PlayerData actor, String reason) throws SQLException {
		return ban(new PlayerBanData(player, actor, reason));
	}

	/**
	 * Temporarily ban a player
	 * You must handle kicking the player if they are online.
	 *
	 * @param player   - Player to ban
	 * @param actor    - Who the ban is by
	 * @param reason   - Why they are banned
	 * @param expires  - Unix Timestamp stating the time of when the ban ends
	 * @throws SQLException 
	 */
	public static boolean ban(PlayerData player, PlayerData actor, String reason, long expires) throws SQLException {
		return ban(new PlayerBanData(player, actor, reason, expires));
	}

	/**
	 * @param ban
	 * @param actor
	 * @return
	 * @throws SQLException
	 */
	public static boolean unban(PlayerBanData ban, PlayerData actor) throws SQLException {
		return plugin.getPlayerBanStorage().unban(ban, actor);
	}

	/**
	 * Thread safe
	 * @param uuid
	 * @return
	 */
	public static boolean isBanned(UUID uuid) {
		return plugin.getPlayerBanStorage().isBanned(uuid);
	}
	
	/**
	 * Thread safe
	 * @param player
	 * @return
	 */
	public static boolean isBanned(Player player) {
		return isBanned(player.getUniqueId());
	}
	
	/**
	 * Thread safe
	 * @param name
	 * @return
	 */
	public static boolean isBanned(String name) {
		return plugin.getPlayerBanStorage().isBanned(name);
	}


	/**
	 * Thread safe
	 * @param name
	 * @return
	 */
	public static PlayerBanData getCurrentBan(String name) {
		return plugin.getPlayerBanStorage().getBan(name);
	}
	
	/**
	 * Thread safe
	 * @param uuid
	 * @return
	 */
	public static PlayerBanData getCurrentBan(UUID uuid) {
		return plugin.getPlayerBanStorage().getBan(uuid);
	}
	
	/**
	 * Thread safe
	 * @param uuid
	 * @return
	 */
	public static PlayerBanData getCurrentBan(Player player) {
		return plugin.getPlayerBanStorage().getBan(player.getUniqueId());
	}

	
	/**
	 * @param player
	 * @return
	 * @throws SQLException
	 */
	public static CloseableIterator<PlayerBanRecord> getBanRecords(PlayerData player) throws SQLException {
		return plugin.getPlayerBanRecordStorage().getRecords(player);
	}

	/**
	 * Permanently mute a player.
	 * You must handle kicking the player if they are online.
	 *
	 * @param mute - PlayerMuteData
	 * @throws SQLException 
	 */
	public static boolean mute(PlayerMuteData mute) throws SQLException {
		return plugin.getPlayerMuteStorage().mute(mute);
	}
	
	/**
	 * Permanently mute a player.
	 * You must handle kicking the player if they are online.
	 *
	 * @param player   - Player to mute
	 * @param actor    - Who the mute is by
	 * @param reason   - Why they are mutened
	 * @throws SQLException 
	 */
	public static boolean mute(PlayerData player, PlayerData actor, String reason) throws SQLException {
		return mute(new PlayerMuteData(player, actor, reason));
	}

	/**
	 * Temporarily mute a player
	 * You must handle kicking the player if they are online.
	 *
	 * @param player   - Player to mute
	 * @param actor    - Who the mute is by
	 * @param reason   - Why they are mutened
	 * @param expires  - Unix Timestamp stating the time of when the mute ends
	 * @throws SQLException 
	 */
	public static boolean mute(PlayerData player, PlayerData actor, String reason, long expires) throws SQLException {
		return mute(new PlayerMuteData(player, actor, reason, expires));
	}

	/**
	 * @param mute
	 * @param actor
	 * @return
	 * @throws SQLException
	 */
	public static boolean unmute(PlayerMuteData mute, PlayerData actor) throws SQLException {
		return plugin.getPlayerMuteStorage().unmute(mute, actor);
	}

	/**
	 * Thread safe
	 * @param uuid
	 * @return
	 */
	public static boolean isMuted(UUID uuid) {
		return plugin.getPlayerMuteStorage().isMuted(uuid);
	}
	
	/**
	 * Thread safe
	 * @param player
	 * @return
	 */
	public static boolean isMuted(Player player) {
		return isMuted(player.getUniqueId());
	}
	
	/**
	 * Thread safe
	 * @param name
	 * @return
	 */
	public static boolean isMuted(String name) {
		return plugin.getPlayerMuteStorage().isMuted(name);
	}


	/**
	 * Thread safe
	 * @param name
	 * @return
	 */
	public static PlayerMuteData getCurrentMute(String name) {
		return plugin.getPlayerMuteStorage().getMute(name);
	}
	
	/**
	 * Thread safe
	 * @param uuid
	 * @return
	 */
	public static PlayerMuteData getCurrentMute(UUID uuid) {
		return plugin.getPlayerMuteStorage().getMute(uuid);
	}
	
	/**
	 * Thread safe
	 * @param uuid
	 * @return
	 */
	public static PlayerMuteData getCurrentMute(Player player) {
		return plugin.getPlayerMuteStorage().getMute(player.getUniqueId());
	}

	
	/**
	 * @param player
	 * @return
	 * @throws SQLException
	 */
	public static CloseableIterator<PlayerMuteRecord> getMuteRecords(PlayerData player) throws SQLException {
		return plugin.getPlayerMuteRecordStorage().getRecords(player);
	}

	/**
	 * Permanently ban an ip.
	 * You must handle kicking the player if they are online.
	 *
	 * @param ban - IpBanData
	 * @throws SQLException 
	 */
	public static boolean ban(IpBanData ban) throws SQLException {
		return plugin.getIpBanStorage().ban(ban);
	}
	
	/**
	 * Permanently ban an ip.
	 * You must handle kicking the player if they are online.
	 *
	 * @param ip       - IP to ban, use ipToLong to convert x.x.x.x to Long
	 * @param actor    - Who the ban is by
	 * @param reason   - Why they are banned
	 * @throws SQLException 
	 */
	public static boolean ban(long ip, PlayerData actor, String reason) throws SQLException {
		return ban(new IpBanData(ip, actor, reason));
	}

	/**
	 * Temporarily ban an ip
	 * You must handle kicking the player if they are online.
	 *
	 * @param ip       - IP to ban, use ipToLong to convert x.x.x.x to Long
	 * @param actor    - Who the ban is by
	 * @param reason   - Why they are banned
	 * @param expires  - Unix Timestamp stating the time of when the ban ends
	 * @throws SQLException 
	 */
	public static boolean ban(long ip, PlayerData actor, String reason, long expires) throws SQLException {
		return ban(new IpBanData(ip, actor, reason, expires));
	}

	/**
	 * @param ban
	 * @param actor
	 * @return
	 * @throws SQLException
	 */
	public static boolean unban(IpBanData ban, PlayerData actor) throws SQLException {
		return plugin.getIpBanStorage().unban(ban, actor);
	}

	/**
	 * Thread safe
	 * @param ip - IP to ban, use ipToLong to convert x.x.x.x to Long
	 * @return
	 */
	public static boolean isBanned(long ip) {
		return plugin.getIpBanStorage().isBanned(ip);
	}

	/**
	 * Thread safe
	 * @param name
	 * @return
	 */
	public static IpBanData getCurrentBan(long ip) {
		return plugin.getIpBanStorage().getBan(ip);
	}
	
	/**
	 * @param player
	 * @return
	 * @throws SQLException
	 */
	public static CloseableIterator<IpBanRecord> getBanRecords(long ip) throws SQLException {
		return plugin.getIpBanRecordStorage().getRecords(ip);
	}

	/**
	 * Warn a player.
	 * You must handle the notification to the warned player yourself.
	 * @param player
	 * @param actor
	 * @param reason
	 * @param read
	 * @throws SQLException
	 */
	public static void warn(PlayerData player, PlayerData actor, String reason, boolean read) throws SQLException {
		plugin.getPlayerWarnStorage().addWarning(new PlayerWarnData(player, actor, reason, read));
	}
	
	/**
	 * Warn a player.
	 * You must handle the notification to the warned player yourself.
	 * @param data
	 * @throws SQLException
	 */
	public static void warn(PlayerWarnData data) throws SQLException {
		plugin.getPlayerWarnStorage().addWarning(data);
	}

	/**
	 * Retrieve past warnings of a player.
	 * This method is not thread safe and should not be called on the main thread.
	 *
	 * @param player
	 * @return ArrayList containing data on all warnings
	 * @throws SQLException 
	 */
	public static CloseableIterator<PlayerWarnData> getWarnings(PlayerData player) throws SQLException {
		return plugin.getPlayerWarnStorage().getWarnings(player);
	}

	/**
	 * @param message - The message config node
	 * @return String
	 */
	public static Message getMessage(String key) {
		return Message.get(key);
	}

}
