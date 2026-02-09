package me.confuser.banmanager.common.storage.conversion;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ipaddr.IPAddressSeqRange;
import me.confuser.banmanager.common.ipaddr.IPAddressString;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

public class LiteBans implements IConverter {
  private ConnectionSource connection;
  private BanManagerPlugin plugin;
  private String tablePrefix;

  private int importedBans = 0;
  private int importedBanRecords = 0;
  private int importedMutes = 0;
  private int importedMuteRecords = 0;
  private int importedWarnings = 0;
  private int importedKicks = 0;
  private int importedHistory = 0;
  private int skipped = 0;

  /**
   * Constructor for testing - uses an existing connection source.
   */
  public LiteBans(BanManagerPlugin plugin, ConnectionSource connection, String tablePrefix) {
    this.plugin = plugin;
    this.connection = connection;
    this.tablePrefix = tablePrefix;

    plugin.getLogger().info("Using existing connection for LiteBans import...");

    runImport();

    // Don't close external connections
    logResults();
  }

  public LiteBans(BanManagerPlugin plugin, String[] args) {
    this.plugin = plugin;

    if (args.length < 5) {
      plugin.getLogger().severe("Usage: /bmimport litebans <host> <port> <databaseName> <username> [password] [tablePrefix]");
      return;
    }

    String host = args[1];
    int port;
    try {
      port = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {
      plugin.getLogger().severe("Invalid port number: " + args[2]);
      return;
    }
    String database = args[3];
    String username = args[4];
    String password = args.length >= 6 ? args[5] : "";
    this.tablePrefix = args.length >= 7 ? args[6] : "litebans_";

    LiteBansConfig config = new LiteBansConfig(
        "mysql", host, port, database, username, password,
        false, false, true, true, 2, 0, 1800000, 30000,
        new HashMap<>(), plugin.getDataFolder()
    );

    try {
      connection = plugin.createConnection(config, "litebans-import");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to connect to LiteBans database");
      return;
    }

    plugin.getLogger().info("Connected to LiteBans database, starting import...");

    runImport();

    connection.closeQuietly();

    logResults();
  }

  /**
   * Run all imports using the interface methods.
   */
  private void runImport() {
    importPlayerBans();
    importIpBans();
    importIpRangeBans();
    importPlayerMutes();
    importIpMutes();
    importPlayerWarnings();
    importKicks();
    importHistory();
  }

  private void logResults() {
    plugin.getLogger().info("LiteBans import complete:");
    plugin.getLogger().info("  - Bans: " + importedBans + " active, " + importedBanRecords + " records");
    plugin.getLogger().info("  - Mutes: " + importedMutes + " active, " + importedMuteRecords + " records");
    plugin.getLogger().info("  - Warnings: " + importedWarnings);
    plugin.getLogger().info("  - Kicks: " + importedKicks);
    plugin.getLogger().info("  - History: " + importedHistory);
    plugin.getLogger().info("  - Skipped: " + skipped);
  }

  /**
   * Resolve an actor from LiteBans UUID and name fields.
   * Handles CONSOLE, invalid UUIDs, and name lookups.
   */
  private PlayerData resolveActor(String uuidStr, String name) {
    if (uuidStr == null || uuidStr.equals("CONSOLE") || uuidStr.isEmpty()) {
      return plugin.getPlayerStorage().getConsole();
    }

    if (uuidStr.equals("#offline#")) {
      // Try to lookup by name
      if (name != null && !name.isEmpty()) {
        PlayerData actor = plugin.getPlayerStorage().retrieve(name, false);
        if (actor != null) return actor;
      }
      return plugin.getPlayerStorage().getConsole();
    }

    if (uuidStr.length() != 36) {
      plugin.getLogger().warning("Invalid operator UUID '" + uuidStr + "', attempting name lookup");
      if (name != null && !name.isEmpty()) {
        PlayerData actor = plugin.getPlayerStorage().retrieve(name, false);
        if (actor != null) return actor;
      }
      return plugin.getPlayerStorage().getConsole();
    }

    try {
      UUID uuid = UUID.fromString(uuidStr);
      // Use the UUID to create or get the actor, with the name as a fallback identifier
      PlayerData actor = plugin.getPlayerStorage().createIfNotExists(uuid, name != null ? name : "Unknown");
      if (actor != null) return actor;
    } catch (IllegalArgumentException e) {
      plugin.getLogger().warning("Could not parse UUID '" + uuidStr + "'");
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to create/get actor with UUID '" + uuidStr + "'");
    }

    return plugin.getPlayerStorage().getConsole();
  }

  /**
   * Truncate reason to 256 characters (BanManager limit).
   */
  private String truncateReason(String reason) {
    if (reason == null) return "";
    if (reason.length() > 256) {
      plugin.getLogger().info("Truncated reason from " + reason.length() + " to 256 chars");
      return reason.substring(0, 256);
    }
    return reason;
  }

  /**
   * Parse IP address from string, handling LiteBans special values.
   */
  private IPAddress parseIp(String ipString) {
    if (ipString == null || ipString.equals("#offline#") || ipString.equals("#") || ipString.equals("#undefined#")) {
      return null;
    }

    try {
      IPAddressString ipAddrStr = new IPAddressString(ipString);
      if (ipAddrStr.isValid()) {
        return ipAddrStr.getAddress();
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Get IP range from wildcard string.
   */
  private IPAddressSeqRange getIpRange(String ipString) {
    if (ipString == null) return null;

    try {
      IPAddressString ipAddrStr = new IPAddressString(ipString);
      if (ipAddrStr.isSequential()) {
        return ipAddrStr.getSequentialRange();
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Convert LiteBans expiry to BanManager format.
   * LiteBans uses milliseconds, -1 or 0 for permanent.
   * BanManager uses seconds, 0 for permanent.
   */
  private long convertExpiry(long liteBansUntil) {
    if (liteBansUntil == -1L || liteBansUntil == 0L) {
      return 0L; // Permanent
    }
    return liteBansUntil / 1000L;
  }

  @Override
  public void importPlayerBans() {
    plugin.getLogger().info("Importing player bans from " + tablePrefix + "bans...");

    DatabaseConnection read;
    try {
      read = connection.getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to get database connection for player bans import");
      return;
    }

    // Only select player bans (ipban = 0)
    String sql = "SELECT `id`, `uuid`, `reason`, `banned_by_uuid`, `banned_by_name`, " +
        "`removed_by_uuid`, `removed_by_name`, `time`, `until`, " +
        "`silent`, `active` FROM `" + tablePrefix + "bans` WHERE `ipban` = 0";

    DatabaseResults results;
    try {
      results = read.compileStatement(sql, StatementBuilder.StatementType.SELECT, null,
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false).runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to query LiteBans bans table for player bans");
      read.closeQuietly();
      return;
    }

    try {
      while (results.next()) {
        processPlayerBanRow(results);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      results.closeQuietly();
      read.closeQuietly();
    }
  }

  private void processPlayerBanRow(DatabaseResults results) throws SQLException {
    int id = results.getInt(0);
    String uuidStr = results.getString(1);
    String reason = truncateReason(results.getString(2));
    String bannedByUuid = results.getString(3);
    String bannedByName = results.getString(4);
    String removedByUuid = results.getString(5);
    String removedByName = results.getString(6);
    long time = results.getLong(7);
    long until = results.getLong(8);
    boolean silent = results.getBoolean(9);
    boolean active = results.getBoolean(10);

    if (uuidStr == null || uuidStr.equals("#offline#")) {
      plugin.getLogger().warning("Skipping player ban " + id + " with invalid UUID: " + uuidStr);
      skipped++;
      return;
    }

    PlayerData actor = resolveActor(bannedByUuid, bannedByName);
    long created = time / 1000L;
    long expires = convertExpiry(until);

    if (active) {
      try {
        UUID uuid = UUID.fromString(uuidStr);

        if (!plugin.getPlayerBanStorage().isBanned(uuid)) {
          PlayerData player = plugin.getPlayerStorage().createIfNotExists(uuid, "Unknown");
          PlayerBanData data = new PlayerBanData(player, actor, reason, silent, expires, created);
          plugin.getPlayerBanStorage().ban(data);
          importedBans++;
        }
      } catch (IllegalArgumentException e) {
        plugin.getLogger().warning("Skipping player ban " + id + " - invalid UUID: " + uuidStr);
        skipped++;
      } catch (SQLException e) {
        plugin.getLogger().severe("Failed to import player ban " + id);
      }
    } else {
      PlayerData removedByActor = resolveActor(removedByUuid, removedByName);

      try {
        UUID uuid = UUID.fromString(uuidStr);
        PlayerData player = plugin.getPlayerStorage().createIfNotExists(uuid, "Unknown");

        PlayerBanRecord existing = plugin.getPlayerBanRecordStorage().queryBuilder()
            .where().eq("player_id", player)
            .and().eq("pastCreated", created)
            .queryForFirst();

        if (existing != null) {
          skipped++;
          return;
        }

        PlayerBanData tempData = new PlayerBanData(player, actor, reason, silent, expires, created);
        PlayerBanRecord record = new PlayerBanRecord(tempData, removedByActor, "Imported from LiteBans");
        plugin.getPlayerBanRecordStorage().create(record);
        importedBanRecords++;
      } catch (IllegalArgumentException e) {
        skipped++;
      } catch (SQLException e) {
        plugin.getLogger().severe("Failed to import player ban record " + id + ": " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @Override
  public void importIpBans() {
    plugin.getLogger().info("Importing IP bans from " + tablePrefix + "bans...");

    DatabaseConnection read;
    try {
      read = connection.getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to get database connection for IP bans import");
      return;
    }

    // Only select IP bans (ipban = 1, ipban_wildcard = 0)
    String sql = "SELECT `id`, `ip`, `reason`, `banned_by_uuid`, `banned_by_name`, " +
        "`removed_by_uuid`, `removed_by_name`, `time`, `until`, " +
        "`silent`, `active` FROM `" + tablePrefix + "bans` WHERE `ipban` = 1 AND `ipban_wildcard` = 0";

    DatabaseResults results;
    try {
      results = read.compileStatement(sql, StatementBuilder.StatementType.SELECT, null,
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false).runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to query LiteBans bans table for IP bans");
      read.closeQuietly();
      return;
    }

    try {
      while (results.next()) {
        processIpBanRow(results);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      results.closeQuietly();
      read.closeQuietly();
    }
  }

  private void processIpBanRow(DatabaseResults results) throws SQLException {
    int id = results.getInt(0);
    String ipStr = results.getString(1);
    String reason = truncateReason(results.getString(2));
    String bannedByUuid = results.getString(3);
    String bannedByName = results.getString(4);
    String removedByUuid = results.getString(5);
    String removedByName = results.getString(6);
    long time = results.getLong(7);
    long until = results.getLong(8);
    boolean silent = results.getBoolean(9);
    boolean active = results.getBoolean(10);

    IPAddress ip = parseIp(ipStr);
    if (ip == null) {
      plugin.getLogger().warning("Skipping IP ban " + id + " - invalid IP: " + ipStr);
      skipped++;
      return;
    }

    PlayerData actor = resolveActor(bannedByUuid, bannedByName);
    long created = time / 1000L;
    long expires = convertExpiry(until);

    if (active) {
      try {
        if (!plugin.getIpBanStorage().isBanned(ip)) {
          IpBanData data = new IpBanData(ip, actor, reason, silent, expires, created);
          plugin.getIpBanStorage().ban(data);
          importedBans++;
        }
      } catch (SQLException e) {
        plugin.getLogger().severe("Failed to import IP ban " + id);
      }
    } else {
      PlayerData removedByActor = resolveActor(removedByUuid, removedByName);

      try {
        IpBanRecord existing = plugin.getIpBanRecordStorage().queryBuilder()
            .where().eq("ip", ip)
            .and().eq("pastCreated", created)
            .queryForFirst();

        if (existing != null) {
          skipped++;
          return;
        }

        IpBanData tempData = new IpBanData(ip, actor, reason, silent, expires, created);
        IpBanRecord record = new IpBanRecord(tempData, removedByActor, "Imported from LiteBans");
        plugin.getIpBanRecordStorage().create(record);
        importedBanRecords++;
      } catch (SQLException e) {
        plugin.getLogger().severe("Failed to import IP ban record " + id);
      }
    }
  }

  @Override
  public void importIpRangeBans() {
    plugin.getLogger().info("Importing IP range bans from " + tablePrefix + "bans...");

    DatabaseConnection read;
    try {
      read = connection.getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to get database connection for IP range bans import");
      return;
    }

    // Only select IP range bans (ipban = 1, ipban_wildcard = 1)
    String sql = "SELECT `id`, `ip`, `reason`, `banned_by_uuid`, `banned_by_name`, " +
        "`removed_by_uuid`, `removed_by_name`, `time`, `until`, " +
        "`silent`, `active` FROM `" + tablePrefix + "bans` WHERE `ipban` = 1 AND `ipban_wildcard` = 1";

    DatabaseResults results;
    try {
      results = read.compileStatement(sql, StatementBuilder.StatementType.SELECT, null,
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false).runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to query LiteBans bans table for IP range bans");
      read.closeQuietly();
      return;
    }

    try {
      while (results.next()) {
        processIpRangeBanRow(results);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      results.closeQuietly();
      read.closeQuietly();
    }
  }

  private void processIpRangeBanRow(DatabaseResults results) throws SQLException {
    int id = results.getInt(0);
    String ipStr = results.getString(1);
    String reason = truncateReason(results.getString(2));
    String bannedByUuid = results.getString(3);
    String bannedByName = results.getString(4);
    String removedByUuid = results.getString(5);
    String removedByName = results.getString(6);
    long time = results.getLong(7);
    long until = results.getLong(8);
    boolean silent = results.getBoolean(9);
    boolean active = results.getBoolean(10);

    IPAddressSeqRange range = getIpRange(ipStr);
    if (range == null) {
      plugin.getLogger().warning("Skipping IP range ban " + id + " - could not parse range: " + ipStr);
      skipped++;
      return;
    }

    IPAddress fromIp = range.getLower();
    IPAddress toIp = range.getUpper();
    PlayerData actor = resolveActor(bannedByUuid, bannedByName);
    long created = time / 1000L;
    long expires = convertExpiry(until);

    if (active) {
      try {
        if (!plugin.getIpRangeBanStorage().isBanned(fromIp) && !plugin.getIpRangeBanStorage().isBanned(toIp)) {
          IpRangeBanData data = new IpRangeBanData(fromIp, toIp, actor, reason, silent, expires, created);
          plugin.getIpRangeBanStorage().ban(data);
          importedBans++;
        }
      } catch (SQLException e) {
        plugin.getLogger().severe("Failed to import IP range ban " + id);
      }
    } else {
      PlayerData removedByActor = resolveActor(removedByUuid, removedByName);

      try {
        IpRangeBanRecord existing = plugin.getIpRangeBanRecordStorage().queryBuilder()
            .where().eq("fromIp", fromIp)
            .and().eq("toIp", toIp)
            .and().eq("pastCreated", created)
            .queryForFirst();

        if (existing != null) {
          skipped++;
          return;
        }

        IpRangeBanData tempData = new IpRangeBanData(fromIp, toIp, actor, reason, silent, expires, created);
        IpRangeBanRecord record = new IpRangeBanRecord(tempData, removedByActor, "Imported from LiteBans");
        plugin.getIpRangeBanRecordStorage().create(record);
        importedBanRecords++;
      } catch (SQLException e) {
        plugin.getLogger().severe("Failed to import IP range ban record " + id);
      }
    }
  }

  @Override
  public void importPlayerMutes() {
    plugin.getLogger().info("Importing player mutes from " + tablePrefix + "mutes...");

    DatabaseConnection read;
    try {
      read = connection.getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to get database connection for player mutes import");
      return;
    }

    // Only select player mutes (ipban = 0)
    String sql = "SELECT `id`, `uuid`, `reason`, `banned_by_uuid`, `banned_by_name`, " +
        "`removed_by_uuid`, `removed_by_name`, `time`, `until`, " +
        "`silent`, `active` FROM `" + tablePrefix + "mutes` WHERE `ipban` = 0";

    DatabaseResults results;
    try {
      results = read.compileStatement(sql, StatementBuilder.StatementType.SELECT, null,
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false).runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to query LiteBans mutes table for player mutes");
      read.closeQuietly();
      return;
    }

    try {
      while (results.next()) {
        processPlayerMuteRow(results);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      results.closeQuietly();
      read.closeQuietly();
    }
  }

  private void processPlayerMuteRow(DatabaseResults results) throws SQLException {
    int id = results.getInt(0);
    String uuidStr = results.getString(1);
    String reason = truncateReason(results.getString(2));
    String bannedByUuid = results.getString(3);
    String bannedByName = results.getString(4);
    String removedByUuid = results.getString(5);
    String removedByName = results.getString(6);
    long time = results.getLong(7);
    long until = results.getLong(8);
    boolean silent = results.getBoolean(9);
    boolean active = results.getBoolean(10);

    if (uuidStr == null || uuidStr.equals("#offline#")) {
      plugin.getLogger().warning("Skipping player mute " + id + " with invalid UUID: " + uuidStr);
      skipped++;
      return;
    }

    PlayerData actor = resolveActor(bannedByUuid, bannedByName);
    long created = time / 1000L;
    long expires = convertExpiry(until);

    if (active) {
      try {
        UUID uuid = UUID.fromString(uuidStr);

        if (!plugin.getPlayerMuteStorage().isMuted(uuid)) {
          PlayerData player = plugin.getPlayerStorage().createIfNotExists(uuid, "Unknown");
          PlayerMuteData data = new PlayerMuteData(player, actor, reason, silent, false, expires, created);
          plugin.getPlayerMuteStorage().mute(data);
          importedMutes++;
        }
      } catch (IllegalArgumentException e) {
        plugin.getLogger().warning("Skipping player mute " + id + " - invalid UUID: " + uuidStr);
        skipped++;
      } catch (SQLException e) {
        plugin.getLogger().severe("Failed to import player mute " + id);
      }
    } else {
      PlayerData removedByActor = resolveActor(removedByUuid, removedByName);

      try {
        UUID uuid = UUID.fromString(uuidStr);
        PlayerData player = plugin.getPlayerStorage().createIfNotExists(uuid, "Unknown");

        PlayerMuteRecord existing = plugin.getPlayerMuteRecordStorage().queryBuilder()
            .where().eq("player_id", player)
            .and().eq("pastCreated", created)
            .queryForFirst();

        if (existing != null) {
          skipped++;
          return;
        }

        PlayerMuteData tempData = new PlayerMuteData(player, actor, reason, silent, false, expires, created);
        PlayerMuteRecord record = new PlayerMuteRecord(tempData, removedByActor, "Imported from LiteBans");
        plugin.getPlayerMuteRecordStorage().create(record);
        importedMuteRecords++;
      } catch (IllegalArgumentException e) {
        skipped++;
      } catch (SQLException e) {
        plugin.getLogger().severe("Failed to import player mute record " + id);
      }
    }
  }

  /**
   * Import IP mutes from litebans_mutes table.
   * Note: Wildcard IP mutes are skipped as BanManager doesn't support them.
   */
  private void importIpMutes() {
    plugin.getLogger().info("Importing IP mutes from " + tablePrefix + "mutes...");

    DatabaseConnection read;
    try {
      read = connection.getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to get database connection for IP mutes import");
      return;
    }

    // Only select IP mutes (ipban = 1, ipban_wildcard = 0) - wildcard IP mutes not supported
    String sql = "SELECT `id`, `ip`, `reason`, `banned_by_uuid`, `banned_by_name`, " +
        "`removed_by_uuid`, `removed_by_name`, `time`, `until`, " +
        "`silent`, `active` FROM `" + tablePrefix + "mutes` WHERE `ipban` = 1 AND `ipban_wildcard` = 0";

    DatabaseResults results;
    try {
      results = read.compileStatement(sql, StatementBuilder.StatementType.SELECT, null,
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false).runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to query LiteBans mutes table for IP mutes");
      read.closeQuietly();
      return;
    }

    try {
      while (results.next()) {
        processIpMuteRow(results);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      results.closeQuietly();
      read.closeQuietly();
    }
  }

  private void processIpMuteRow(DatabaseResults results) throws SQLException {
    int id = results.getInt(0);
    String ipStr = results.getString(1);
    String reason = truncateReason(results.getString(2));
    String bannedByUuid = results.getString(3);
    String bannedByName = results.getString(4);
    String removedByUuid = results.getString(5);
    String removedByName = results.getString(6);
    long time = results.getLong(7);
    long until = results.getLong(8);
    boolean silent = results.getBoolean(9);
    boolean active = results.getBoolean(10);

    IPAddress ip = parseIp(ipStr);
    if (ip == null) {
      plugin.getLogger().warning("Skipping IP mute " + id + " - invalid IP: " + ipStr);
      skipped++;
      return;
    }

    PlayerData actor = resolveActor(bannedByUuid, bannedByName);
    long created = time / 1000L;
    long expires = convertExpiry(until);

    if (active) {
      try {
        if (!plugin.getIpMuteStorage().isMuted(ip)) {
          IpMuteData data = new IpMuteData(ip, actor, reason, silent, false, expires, created);
          plugin.getIpMuteStorage().mute(data);
          importedMutes++;
        }
      } catch (SQLException e) {
        plugin.getLogger().severe("Failed to import IP mute " + id);
      }
    } else {
      PlayerData removedByActor = resolveActor(removedByUuid, removedByName);

      try {
        IpMuteRecord existing = plugin.getIpMuteRecordStorage().queryBuilder()
            .where().eq("ip", ip)
            .and().eq("pastCreated", created)
            .queryForFirst();

        if (existing != null) {
          skipped++;
          return;
        }

        IpMuteData tempData = new IpMuteData(ip, actor, reason, silent, false, expires, created);
        IpMuteRecord record = new IpMuteRecord(tempData, removedByActor, "Imported from LiteBans");
        plugin.getIpMuteRecordStorage().create(record);
        importedMuteRecords++;
      } catch (SQLException e) {
        plugin.getLogger().severe("Failed to import IP mute record " + id);
      }
    }
  }

  @Override
  public void importPlayerWarnings() {
    plugin.getLogger().info("Importing warnings from " + tablePrefix + "warnings...");

    DatabaseConnection read;
    try {
      read = connection.getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to get database connection for warnings import");
      return;
    }

    String sql = "SELECT `id`, `uuid`, `reason`, `banned_by_uuid`, `banned_by_name`, " +
        "`time`, `until`, `warned` FROM `" + tablePrefix + "warnings`";

    DatabaseResults results;
    try {
      results = read.compileStatement(sql, StatementBuilder.StatementType.SELECT, null,
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false).runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to query LiteBans warnings table");
      read.closeQuietly();
      return;
    }

    try {
      while (results.next()) {
        int id = results.getInt(0);
        String uuidStr = results.getString(1);
        String reason = truncateReason(results.getString(2));
        String bannedByUuid = results.getString(3);
        String bannedByName = results.getString(4);
        long time = results.getLong(5);
        long until = results.getLong(6);
        boolean warned = results.getBoolean(7);

        if (uuidStr == null || uuidStr.equals("#offline#")) {
          plugin.getLogger().warning("Skipping warning " + id + " with invalid UUID: " + uuidStr);
          skipped++;
          continue;
        }

        try {
          UUID uuid = UUID.fromString(uuidStr);
          // Create or get player by UUID - use "Unknown" as name since LiteBans doesn't store warned player name
          PlayerData player = plugin.getPlayerStorage().createIfNotExists(uuid, "Unknown");

          PlayerData actor = resolveActor(bannedByUuid, bannedByName);
          long created = time / 1000L;
          long expires = convertExpiry(until);

          // Check for duplicate: same player, actor, and created timestamp
          PlayerWarnData existing = plugin.getPlayerWarnStorage().queryBuilder()
              .where().eq("player_id", player)
              .and().eq("actor_id", actor)
              .and().eq("created", created)
              .queryForFirst();

          if (existing != null) {
            skipped++;
            continue;
          }

          // Map 'warned' to 'read' - if player was warned, they've seen it
          PlayerWarnData data = new PlayerWarnData(player, actor, reason, warned, expires, created);
          plugin.getPlayerWarnStorage().create(data);
          importedWarnings++;
        } catch (IllegalArgumentException e) {
          plugin.getLogger().warning("Skipping warning " + id + " - invalid UUID: " + uuidStr);
          skipped++;
        } catch (SQLException e) {
          plugin.getLogger().severe("Failed to import warning " + id);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      results.closeQuietly();
      read.closeQuietly();
    }
  }

  /**
   * Import kicks from litebans_kicks table.
   */
  private void importKicks() {
    plugin.getLogger().info("Importing kicks from " + tablePrefix + "kicks...");

    DatabaseConnection read;
    try {
      read = connection.getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to get database connection for kicks import");
      return;
    }

    String sql = "SELECT `id`, `uuid`, `reason`, `banned_by_uuid`, `banned_by_name`, " +
        "`time` FROM `" + tablePrefix + "kicks`";

    DatabaseResults results;
    try {
      results = read.compileStatement(sql, StatementBuilder.StatementType.SELECT, null,
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false).runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to query LiteBans kicks table");
      read.closeQuietly();
      return;
    }

    try {
      while (results.next()) {
        int id = results.getInt(0);
        String uuidStr = results.getString(1);
        String reason = truncateReason(results.getString(2));
        String bannedByUuid = results.getString(3);
        String bannedByName = results.getString(4);
        long time = results.getLong(5);

        if (uuidStr == null || uuidStr.equals("#offline#")) {
          plugin.getLogger().warning("Skipping kick " + id + " with invalid UUID: " + uuidStr);
          skipped++;
          continue;
        }

        try {
          UUID uuid = UUID.fromString(uuidStr);
          // Create or get player by UUID
          PlayerData player = plugin.getPlayerStorage().createIfNotExists(uuid, "Unknown");

          PlayerData actor = resolveActor(bannedByUuid, bannedByName);
          long created = time / 1000L;

          // Check for duplicate: same player, actor, and created timestamp
          PlayerKickData existing = plugin.getPlayerKickStorage().queryBuilder()
              .where().eq("player_id", player)
              .and().eq("actor_id", actor)
              .and().eq("created", created)
              .queryForFirst();

          if (existing != null) {
            skipped++;
            continue;
          }

          PlayerKickData data = new PlayerKickData(player, actor, reason, created);
          plugin.getPlayerKickStorage().create(data);
          importedKicks++;
        } catch (IllegalArgumentException e) {
          plugin.getLogger().warning("Skipping kick " + id + " - invalid UUID: " + uuidStr);
          skipped++;
        } catch (SQLException e) {
          plugin.getLogger().severe("Failed to import kick " + id);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      results.closeQuietly();
      read.closeQuietly();
    }
  }

  /**
   * Import history from litebans_history table.
   */
  private void importHistory() {
    plugin.getLogger().info("Importing history from " + tablePrefix + "history...");

    DatabaseConnection read;
    try {
      read = connection.getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to get database connection for history import");
      return;
    }

    String sql = "SELECT `id`, `uuid`, `name`, `ip`, `date` FROM `" + tablePrefix + "history`";

    DatabaseResults results;
    try {
      results = read.compileStatement(sql, StatementBuilder.StatementType.SELECT, null,
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false).runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to query LiteBans history table");
      read.closeQuietly();
      return;
    }

    try {
      while (results.next()) {
        int id = results.getInt(0);
        String uuidStr = results.getString(1);
        String name = results.getString(2);
        String ipStr = results.getString(3);
        Timestamp date = results.getTimestamp(4);

        if (uuidStr == null || uuidStr.equals("#offline#")) {
          skipped++;
          continue;
        }

        try {
          UUID uuid = UUID.fromString(uuidStr);
          // Create or get player by UUID and name from history
          PlayerData player = plugin.getPlayerStorage().createIfNotExists(uuid, name != null ? name : "Unknown");

          // Parse IP if valid
          IPAddress ip = null;
          if (ipStr != null && !ipStr.equals("#") && !ipStr.equals("#offline#") && !ipStr.equals("#undefined#")) {
            ip = parseIp(ipStr);
          }

          long timestamp = date != null ? date.getTime() / 1000L : System.currentTimeMillis() / 1000L;

          // Check for duplicate: same player and join timestamp
          PlayerHistoryData existing = plugin.getPlayerHistoryStorage().queryBuilder()
              .where().eq("player_id", player)
              .and().eq("join", timestamp)
              .queryForFirst();

          if (existing != null) {
            skipped++;
            continue;
          }

          // Create history entry with the parsed IP address and timestamps
          PlayerHistoryData data = new PlayerHistoryData(player, ip, timestamp, timestamp);

          plugin.getPlayerHistoryStorage().create(data);
          importedHistory++;
        } catch (IllegalArgumentException e) {
          skipped++;
        } catch (SQLException e) {
          plugin.getLogger().severe("Failed to import history " + id);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      results.closeQuietly();
      read.closeQuietly();
    }
  }

  /**
   * Config class for LiteBans database connection.
   */
  static class LiteBansConfig extends DatabaseConfig {
    public LiteBansConfig(String storageType, String host, int port, String name, String user, String password,
                          boolean useSSL, boolean verifyServerCertificate, boolean allowPublicKeyRetrieval,
                          boolean isEnabled, int maxConnections, int leakDetection, int maxLifetime,
                          int connectionTimeout, HashMap<String, DatabaseTableConfig<?>> tables, File dataFolder) {
      super(storageType, host, port, name, user, password, useSSL, verifyServerCertificate, allowPublicKeyRetrieval,
          isEnabled, maxConnections, leakDetection, maxLifetime, connectionTimeout, tables, dataFolder);
    }
  }
}
