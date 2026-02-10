package me.confuser.banmanager.common.api;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This is a static API for BanManager
 * No methods are thread safe unless stated otherwise
 * Note: The API does not handle permission checks for exemptions
 *
 * @author James Mortemore
 */
public class BmAPI {

  /**
   * @param uuid Player UUID
   * @return PlayerData
   * @throws SQLException
   */
  public static PlayerData getPlayer(UUID uuid) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
  }

  /**
   * @param name Player name
   * @return PlayerData
   * @throws SQLException
   */
  public static PlayerData getPlayer(String name) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerStorage().retrieve(name, false);
  }

  /**
   * @param ip Player IP
   * @return a list of PlayerData
   * @throws SQLException
   */
  public static List<PlayerData> getPlayers(IPAddress ip) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerStorage().getDuplicatesInTime(ip, BanManagerPlugin.getInstance().getConfig().getTimeAssociatedAlts());
  }

  /**
   * Get the console for use as an actor
   *
   * @return PlayerData
   */
  public static PlayerData getConsole() {
    return BanManagerPlugin.getInstance().getPlayerStorage().getConsole();
  }

  /**
   * Convert an ip string of x.x.x.x to IPAddress
   *
   * @param ip IPv4 in x.x.x.x format
   * @return IPv4 in number format
   */
  public static IPAddress toIp(String ip) {
    return IPUtils.toIPAddress(ip);
  }

  /**
   * Permanently ban a player.
   * You must handle kicking the player if they are online.
   *
   * @param ban    PlayerBanData
   * @return Returns true if ban successful
   * @throws SQLException
   */
  public static boolean ban(PlayerBanData ban) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerBanStorage().ban(ban);
  }

  /**
   * Permanently ban a player.
   * You must handle kicking the player if they are online.
   *
   * @param player Player to ban
   * @param actor  Who the ban is by
   * @param reason Why they are banned
   * @param silent Whether the ban should be broadcast
   * @return Returns true if ban successful
   * @throws SQLException
   */
  public static boolean ban(PlayerData player, PlayerData actor, String reason, boolean silent) throws SQLException {
    return ban(new PlayerBanData(player, actor, reason, silent));
  }

  /**
   * Temporarily ban a player
   * You must handle kicking the player if they are online.
   *
   * @param player  Player to ban
   * @param actor   Who the ban is by
   * @param reason  Why they are banned
   * @param expires Unix Timestamp in seconds stating the time of when the ban ends
   * @return Returns true if ban successful
   * @throws SQLException
   */
  public static boolean ban(PlayerData player, PlayerData actor, String reason, boolean silent, long expires) throws SQLException {
    return ban(new PlayerBanData(player, actor, reason, silent, expires));
  }

  /**
   * @param ban   The ban (can be retrieved via getBan)
   * @param actor Who is unbanning the player
   * @return Returns true if the unban is successful
   * @throws SQLException
   */
  public static boolean unban(PlayerBanData ban, PlayerData actor) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerBanStorage().unban(ban, actor);
  }

  /**
   * @param ban   The ban (can be retrieved via getBan)
   * @param actor Who is unbanning the player
   * @param silent Whether the unban should be treated as silent
   * @return Returns true if the unban is successful
   * @throws SQLException
   */
  public static boolean unban(PlayerBanData ban, PlayerData actor, boolean silent) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerBanStorage().unban(ban, actor, "", false, silent);
  }

  /**
   * Thread safe
   *
   * @param uuid Player UUID
   * @return Returns true if player is banned
   */
  public static boolean isBanned(UUID uuid) {
    return BanManagerPlugin.getInstance().getPlayerBanStorage().isBanned(uuid);
  }

  /**
   * Thread safe
   *
   * @param name Player name
   * @return Returns true if player is banned
   */
  public static boolean isBanned(String name) {
    return BanManagerPlugin.getInstance().getPlayerBanStorage().isBanned(name);
  }


  /**
   * Thread safe
   *
   * @param name Player name
   * @return Returns the active ban of a player; if the player is not banned this returns null
   */
  public static PlayerBanData getCurrentBan(String name) {
    return BanManagerPlugin.getInstance().getPlayerBanStorage().getBan(name);
  }

  /**
   * Thread safe
   *
   * @param uuid Player UUID
   * @return Returns the active ban of a player; if the player is not banned this returns null
   */
  public static PlayerBanData getCurrentBan(UUID uuid) {
    return BanManagerPlugin.getInstance().getPlayerBanStorage().getBan(uuid);
  }

  /**
   * @param player BanManager's player record
   * @return An iterator of ban records
   * @throws SQLException
   */
  public static CloseableIterator<PlayerBanRecord> getBanRecords(PlayerData player) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerBanRecordStorage().getRecords(player);
  }

  /**
   * Permanently mute a player.
   * You must handle kicking the player if they are online.
   *
   * @param mute   PlayerMuteData
   * @return Returns true if the mute is successful
   * @throws SQLException
   */
  public static boolean mute(PlayerMuteData mute) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerMuteStorage().mute(mute);
  }

  /**
   * Permanently mute a player.
   * You must handle kicking the player if they are online.
   *
   * @param player Player to mute
   * @param actor  Who the mute is by
   * @param reason Why they are muted
   * @return Returns true if the mute is successful
   * @throws SQLException
   */
  public static boolean mute(PlayerData player, PlayerData actor, String reason) throws SQLException {
    return mute(new PlayerMuteData(player, actor, reason, false, false));
  }

  /**
   * Permanently mute a player.
   * You must handle kicking the player if they are online.
   *
   * @param player Player to mute
   * @param actor  Who the mute is by
   * @param reason Why they are mutened
   * @param silent Whether the mute should be broadcast
   * @return Returns true if mute is successful
   * @throws SQLException
   */
  public static boolean mute(PlayerData player, PlayerData actor, String reason, boolean silent) throws SQLException {
    return mute(new PlayerMuteData(player, actor, reason, silent, false));
  }

  /**
   * Permanently mute a player.
   * You must handle kicking the player if they are online.
   *
   * @param player Player to mute
   * @param actor  Who the mute is by
   * @param reason Why they are mutened
   * @param silent Whether the mute should be broadcast
   * @param isSoft Whether the player should be aware they are muted; they will still see their own messages but nobody else will
   * @return Returns true if the mute is successful
   * @throws SQLException
   */
  public static boolean mute(PlayerData player, PlayerData actor, String reason, boolean silent, boolean isSoft) throws SQLException {
    return mute(new PlayerMuteData(player, actor, reason, silent, isSoft));
  }


  /**
   * Temporarily mute a player
   * You must handle kicking the player if they are online.
   *
   * @param player  Player to mute
   * @param actor   Who the mute is by
   * @param reason  Why they are mutened
   * @param silent Whether the mute should be broadcast
   * @param isSoft Whether the player should be aware they are muted; they will still see their own messages but nobody else will
   * @param expires Unix Timestamp in seconds stating the time of when the mute ends
   * @return Returns true if mute successful
   * @throws SQLException
   */
  public static boolean mute(PlayerData player, PlayerData actor, String reason, boolean silent, boolean isSoft, long expires) throws SQLException {
    return mute(new PlayerMuteData(player, actor, reason, silent, isSoft, expires));
  }

  /**
   * Mute an IP.
   *
   * @param mute   IpMuteData
   * @return Returns true if the mute is successful
   * @throws SQLException
   */
  public static boolean mute(IpMuteData mute) throws SQLException {
    return BanManagerPlugin.getInstance().getIpMuteStorage().mute(mute);
  }

  /**
   * @param mute  PlayerMuteData
   * @param actor Who unmuted the player
   * @return Returns true if unmute successful
   * @throws SQLException
   */
  public static boolean unmute(PlayerMuteData mute, PlayerData actor) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerMuteStorage().unmute(mute, actor);
  }

  /**
   * @param mute  PlayerMuteData
   * @param actor Who unmuted the player
   * @param silent Whether the unmute should be treated as silent
   * @return Returns true if unmute successful
   * @throws SQLException
   */
  public static boolean unmute(PlayerMuteData mute, PlayerData actor, boolean silent) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerMuteStorage().unmute(mute, actor, "", false, silent);
  }

  /**
   * @param mute   IP Mute record
   * @param actor Who unmuted the ip
   * @return Returns true if unmute is successful
   * @throws SQLException
   */
  public static boolean unmute(IpMuteData mute, PlayerData actor) throws SQLException {
    return BanManagerPlugin.getInstance().getIpMuteStorage().unmute(mute, actor);
  }

  /**
   * @param mute   IP Mute record
   * @param actor Who unmuted the ip
   * @param silent Whether the unmute should be treated as silent
   * @return Returns true if unmute is successful
   * @throws SQLException
   */
  public static boolean unmute(IpMuteData mute, PlayerData actor, boolean silent) throws SQLException {
    return BanManagerPlugin.getInstance().getIpMuteStorage().unmute(mute, actor, "", silent);
  }

  /**
   * Thread safe
   *
   * @param uuid Player UUID
   * @return Returns true if player muted
   */
  public static boolean isMuted(UUID uuid) {
    return BanManagerPlugin.getInstance().getPlayerMuteStorage().isMuted(uuid);
  }

  /**
   * Thread safe
   *
   * @param name Player Name
   * @return Returns true if player muted
   */
  public static boolean isMuted(String name) {
    return BanManagerPlugin.getInstance().getPlayerMuteStorage().isMuted(name);
  }

  /**
   * Thread safe
   *
   * @param ip IP address
   * @return Returns true if IP address muted
   */
  public static boolean isMuted(IPAddress ip) {
    return BanManagerPlugin.getInstance().getIpMuteStorage().isMuted(ip);
  }

  /**
   * Thread safe
   *
   * @param name
   * @return Returns the active mute of a player; if the player is not muted this returns null
   */
  public static PlayerMuteData getCurrentMute(String name) {
    return BanManagerPlugin.getInstance().getPlayerMuteStorage().getMute(name);
  }

  /**
   * Thread safe
   *
   * @param uuid
   * @return Returns the active mute of a player; if the player is not muted this returns null
   */
  public static PlayerMuteData getCurrentMute(UUID uuid) {
    return BanManagerPlugin.getInstance().getPlayerMuteStorage().getMute(uuid);
  }

  /**
   * @param player Player record
   * @return Iterator of previous mutes
   * @throws SQLException
   */
  public static CloseableIterator<PlayerMuteRecord> getMuteRecords(PlayerData player) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerMuteRecordStorage().getRecords(player);
  }

  /**
   * Permanently ban an ip.
   * You must handle kicking the player if they are online.
   *
   * @param ban    IpBanData
   * @return Returns true if ban is successful
   * @throws SQLException
   */
  public static boolean ban(IpBanData ban) throws SQLException {
    return BanManagerPlugin.getInstance().getIpBanStorage().ban(ban);
  }

  /**
   * Permanently ban an ip.
   * You must handle kicking the player if they are online.
   *
   * @param ip     IP to ban, use toIp to convert x.x.x.x to IPAddress
   * @param actor  Who the ban is by
   * @param reason Why they are banned
   * @param silent Whether the ban should be broadcast
   * @return Returns true if ban is successful
   * @throws SQLException
   */
  public static boolean ban(IPAddress ip, PlayerData actor, String reason, boolean silent) throws SQLException {
    return ban(new IpBanData(ip, actor, reason, silent));
  }

  /**
   * Temporarily ban an ip
   * You must handle kicking the player if they are online.
   *
   * @param ip      IP to ban, use toIp to convert x.x.x.x to IPAddress
   * @param actor   Who the ban is by
   * @param reason  Why they are banned
   * @param silent Whether the ban should be broadcast
   * @param expires Unix Timestamp in seconds stating the time of when the ban ends
   * @return Returns true if ban is successful
   * @throws SQLException
   */
  public static boolean ban(IPAddress ip, PlayerData actor, String reason, boolean silent, long expires) throws SQLException {
    return ban(new IpBanData(ip, actor, reason, silent, expires));
  }

  /**
   * @param ban   IP Ban record
   * @param actor Who unbanned the ip
   * @return Returns true if unban is successful
   * @throws SQLException
   */
  public static boolean unban(IpBanData ban, PlayerData actor) throws SQLException {
    return BanManagerPlugin.getInstance().getIpBanStorage().unban(ban, actor);
  }

  /**
   * @param ban   IP Ban record
   * @param actor Who unbanned the ip
   * @param silent Whether the unban should be treated as silent
   * @return Returns true if unban is successful
   * @throws SQLException
   */
  public static boolean unban(IpBanData ban, PlayerData actor, boolean silent) throws SQLException {
    return BanManagerPlugin.getInstance().getIpBanStorage().unban(ban, actor, "", false, silent);
  }

  /**
   * Thread safe
   *
   * @param ip IP to ban, use toIp to convert x.x.x.x to IPAddress
   * @return Returns true if ip is banned
   */
  public static boolean isBanned(IPAddress ip) {
    return BanManagerPlugin.getInstance().getIpBanStorage().isBanned(ip);
  }

  /**
   * Thread safe
   *
   * @param ip IP to ban, use toIp to convert x.x.x.x to IPAddress
   * @return Returns the active ban of an ip if the ip is not banned this returns null
   */
  public static IpBanData getCurrentBan(IPAddress ip) {
    return BanManagerPlugin.getInstance().getIpBanStorage().getBan(ip);
  }

  /**
   * @param ip IP to ban, use toIp to convert x.x.x.x to IPAddress
   * @return Returns previous bans of an ip
   * @throws SQLException
   */
  public static CloseableIterator<IpBanRecord> getBanRecords(IPAddress ip) throws SQLException {
    return BanManagerPlugin.getInstance().getIpBanRecordStorage().getRecords(ip);
  }

  /**
   * Warn a player.
   * You must handle the notification to the warned player yourself.
   *
   * @param player Player record
   * @param actor  Player record of who warned the player
   * @param reason What the player was warned for
   * @param read   Whether the player has already viewed the warning
   * @return Returns true if warning is successful
   * @throws SQLException
   */
  public static boolean warn(PlayerData player, PlayerData actor, String reason, boolean read) throws SQLException {
    return warn(new PlayerWarnData(player, actor, reason, 1, read));
  }

  /**
   * Warn a player.
   * You must handle the notification to the warned player yourself.
   *
   * @param player Player record
   * @param actor  Player record of who warned the player
   * @param reason What the player was warned for
   * @param read   Whether the player has already viewed the warning
   * @param silent Whether the warning should be broadcast
   * @return Returns true if warning is successful
   * @throws SQLException
   */
  public static boolean warn(PlayerData player, PlayerData actor, String reason, boolean read, boolean silent) throws
      SQLException {
    return warn(new PlayerWarnData(player, actor, reason, 1, read), silent);
  }

  /**
   * Warn a player.
   * You must handle the notification to the warned player yourself.
   *
   * @param data PlayerWarnData
   * @return Returns true if warning is successful
   * @throws SQLException
   */
  public static boolean warn(PlayerWarnData data) throws SQLException {
    return warn(data, false);
  }

  /**
   * Warn a player.
   * You must handle the notification to the warned player yourself.
   *
   * @param data   PlayerWarnData
   * @param silent Whether the warning should be broadcast
   * @return Returns true if warning is successful
   * @throws SQLException
   */
  public static boolean warn(PlayerWarnData data, boolean silent) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerWarnStorage().addWarning(data, silent);
  }

  /**
   * Retrieve past warnings of a player.
   * This method is not thread safe and should not be called on the main thread.
   *
   * @param player Player record
   * @return Iterator containing previous player warningss
   * @throws SQLException
   */
  public static CloseableIterator<PlayerWarnData> getWarnings(PlayerData player) throws SQLException {
    return BanManagerPlugin.getInstance().getPlayerWarnStorage().getWarnings(player);
  }

  /**
   * Get all known names for a player (summary view with first/last seen).
   * This method is not thread safe and should not be called on the main thread.
   *
   * @param uuid Player UUID
   * @return List of PlayerNameSummary ordered by lastSeen descending (most recent first)
   * @throws SQLException
   */
  public static List<PlayerNameSummary> getPlayerNames(UUID uuid) throws SQLException {
    PlayerData player = getPlayer(uuid);

    if (player == null) return new ArrayList<>();

    return BanManagerPlugin.getInstance().getPlayerHistoryStorage().getNamesSummary(player);
  }

  /**
   * Get the full session history for a player.
   * This method is not thread safe and should not be called on the main thread.
   *
   * @param uuid Player UUID
   * @param since Unix timestamp in seconds to get sessions since
   * @param page Page number (0-indexed, 10 results per page)
   * @return Iterator of PlayerHistoryData ordered by join descending (most recent first), null if player not found
   * @throws SQLException
   */
  public static CloseableIterator<PlayerHistoryData> getPlayerHistory(UUID uuid, long since, int page) throws SQLException {
    PlayerData player = getPlayer(uuid);

    if (player == null) return null;

    return BanManagerPlugin.getInstance().getPlayerHistoryStorage().getSince(player, since, page);
  }

  /**
   * Get the name a player was using at a specific timestamp.
   * This method is not thread safe and should not be called on the main thread.
   *
   * @param uuid Player UUID
   * @param timestamp Unix timestamp in seconds
   * @return The name at that time, or null if not found
   * @throws SQLException
   */
  public static String getPlayerNameAt(UUID uuid, long timestamp) throws SQLException {
    PlayerData player = getPlayer(uuid);

    if (player == null) return null;

    return BanManagerPlugin.getInstance().getPlayerHistoryStorage().getNameAt(player, timestamp);
  }

  /**
   * @param key The message config node within messages.yml, e.g. "ban.notify"
   * @return String
   */
  public static Message getMessage(String key) {
    return Message.get(key);
  }

  public static ConnectionSource getLocalConnection() {
    return BanManagerPlugin.getInstance().getLocalConn();
  }

  public static long toTimestamp(String time, boolean future) throws Exception {
    return DateUtils.parseDateDiff(time, future);
  }
}
