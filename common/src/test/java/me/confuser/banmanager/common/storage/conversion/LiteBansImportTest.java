package me.confuser.banmanager.common.storage.conversion;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ipaddr.IPAddressString;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Tests for the LiteBans to BanManager import functionality.
 * Creates a mock LiteBans database schema and populates it with test data
 * to verify the import process works correctly.
 */
public class LiteBansImportTest extends BasePluginDbTest {

  private static final String TABLE_PREFIX = "litebans_";

  // Test UUIDs
  private static final String PLAYER_UUID_1 = "550e8400-e29b-41d4-a716-446655440001";
  private static final String PLAYER_UUID_2 = "550e8400-e29b-41d4-a716-446655440002";
  private static final String PLAYER_UUID_3 = "550e8400-e29b-41d4-a716-446655440003";
  private static final String ACTOR_UUID = "550e8400-e29b-41d4-a716-446655440099";

  @Before
  public void setupLiteBansTables() throws SQLException, IOException {
    createLiteBansSchema();
    insertTestData();
  }

  /**
   * Creates the LiteBans table schema in the test database.
   */
  private void createLiteBansSchema() throws SQLException, IOException {
    try (DatabaseConnection conn = plugin.getLocalConn().getReadWriteConnection("")) {
      // Create bans table
      conn.executeStatement(
          "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "bans (" +
              "id INT AUTO_INCREMENT PRIMARY KEY," +
              "uuid VARCHAR(36)," +
              "ip VARCHAR(45)," +
              "reason VARCHAR(2048)," +
              "banned_by_uuid VARCHAR(36)," +
              "banned_by_name VARCHAR(128)," +
              "removed_by_uuid VARCHAR(36)," +
              "removed_by_name VARCHAR(128)," +
              "removed_by_date TIMESTAMP NULL," +
              "time BIGINT," +
              "until BIGINT," +
              "server_scope VARCHAR(32)," +
              "server_origin VARCHAR(32)," +
              "silent BIT DEFAULT 0," +
              "ipban BIT DEFAULT 0," +
              "ipban_wildcard BIT DEFAULT 0," +
              "active BIT DEFAULT 1" +
              ")",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Create mutes table
      conn.executeStatement(
          "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "mutes (" +
              "id INT AUTO_INCREMENT PRIMARY KEY," +
              "uuid VARCHAR(36)," +
              "ip VARCHAR(45)," +
              "reason VARCHAR(2048)," +
              "banned_by_uuid VARCHAR(36)," +
              "banned_by_name VARCHAR(128)," +
              "removed_by_uuid VARCHAR(36)," +
              "removed_by_name VARCHAR(128)," +
              "removed_by_date TIMESTAMP NULL," +
              "time BIGINT," +
              "until BIGINT," +
              "server_scope VARCHAR(32)," +
              "server_origin VARCHAR(32)," +
              "silent BIT DEFAULT 0," +
              "ipban BIT DEFAULT 0," +
              "ipban_wildcard BIT DEFAULT 0," +
              "active BIT DEFAULT 1" +
              ")",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Create warnings table
      conn.executeStatement(
          "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "warnings (" +
              "id INT AUTO_INCREMENT PRIMARY KEY," +
              "uuid VARCHAR(36)," +
              "ip VARCHAR(45)," +
              "reason VARCHAR(2048)," +
              "banned_by_uuid VARCHAR(36)," +
              "banned_by_name VARCHAR(128)," +
              "removed_by_uuid VARCHAR(36)," +
              "removed_by_name VARCHAR(128)," +
              "removed_by_date TIMESTAMP NULL," +
              "time BIGINT," +
              "until BIGINT," +
              "server_scope VARCHAR(32)," +
              "server_origin VARCHAR(32)," +
              "warned BIT DEFAULT 0," +
              "active BIT DEFAULT 1" +
              ")",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Create kicks table
      conn.executeStatement(
          "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "kicks (" +
              "id INT AUTO_INCREMENT PRIMARY KEY," +
              "uuid VARCHAR(36)," +
              "ip VARCHAR(45)," +
              "reason VARCHAR(2048)," +
              "banned_by_uuid VARCHAR(36)," +
              "banned_by_name VARCHAR(128)," +
              "time BIGINT," +
              "until BIGINT," +
              "server_scope VARCHAR(32)," +
              "server_origin VARCHAR(32)," +
              "silent BIT DEFAULT 0" +
              ")",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Create history table
      conn.executeStatement(
          "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "history (" +
              "id INT AUTO_INCREMENT PRIMARY KEY," +
              "uuid VARCHAR(36)," +
              "name VARCHAR(16)," +
              "ip VARCHAR(45)," +
              "date TIMESTAMP" +
              ")",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );
    }
  }

  /**
   * Inserts test data into the LiteBans tables.
   */
  private void insertTestData() throws SQLException, IOException {
    long currentTime = System.currentTimeMillis();
    long pastTime = currentTime - 86400000L; // 1 day ago

    try (DatabaseConnection conn = plugin.getLocalConn().getReadWriteConnection("")) {
      // Active player ban
      conn.executeStatement(
          "INSERT INTO " + TABLE_PREFIX + "bans " +
              "(uuid, ip, reason, banned_by_uuid, banned_by_name, time, until, silent, ipban, ipban_wildcard, active) VALUES " +
              "('" + PLAYER_UUID_1 + "', '192.168.1.1', 'Test ban reason', '" + ACTOR_UUID + "', 'TestActor', " + pastTime + ", 0, 0, 0, 0, 1)",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Active IP ban
      conn.executeStatement(
          "INSERT INTO " + TABLE_PREFIX + "bans " +
              "(uuid, ip, reason, banned_by_uuid, banned_by_name, time, until, silent, ipban, ipban_wildcard, active) VALUES " +
              "('" + PLAYER_UUID_2 + "', '10.0.0.1', 'IP ban reason', 'CONSOLE', 'Console', " + pastTime + ", 0, 1, 1, 0, 1)",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Active wildcard IP ban (IP range)
      conn.executeStatement(
          "INSERT INTO " + TABLE_PREFIX + "bans " +
              "(uuid, ip, reason, banned_by_uuid, banned_by_name, time, until, silent, ipban, ipban_wildcard, active) VALUES " +
              "('" + PLAYER_UUID_3 + "', '172.16.0.0/16', 'Range ban reason', '" + ACTOR_UUID + "', 'TestActor', " + pastTime + ", 0, 0, 1, 1, 1)",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Inactive (removed) player ban - should create a ban record
      conn.executeStatement(
          "INSERT INTO " + TABLE_PREFIX + "bans " +
              "(uuid, ip, reason, banned_by_uuid, banned_by_name, removed_by_uuid, removed_by_name, removed_by_date, time, until, silent, ipban, ipban_wildcard, active) VALUES " +
              "('550e8400-e29b-41d4-a716-446655440004', '192.168.1.4', 'Old ban reason', '" + ACTOR_UUID + "', 'TestActor', 'CONSOLE', 'Console', NOW(), " + pastTime + ", 0, 0, 0, 0, 0)",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Active player mute
      conn.executeStatement(
          "INSERT INTO " + TABLE_PREFIX + "mutes " +
              "(uuid, ip, reason, banned_by_uuid, banned_by_name, time, until, silent, ipban, ipban_wildcard, active) VALUES " +
              "('" + PLAYER_UUID_1 + "', '192.168.1.1', 'Test mute reason', '" + ACTOR_UUID + "', 'TestActor', " + pastTime + ", 0, 0, 0, 0, 1)",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Active IP mute
      conn.executeStatement(
          "INSERT INTO " + TABLE_PREFIX + "mutes " +
              "(uuid, ip, reason, banned_by_uuid, banned_by_name, time, until, silent, ipban, ipban_wildcard, active) VALUES " +
              "('" + PLAYER_UUID_2 + "', '10.0.0.2', 'IP mute reason', 'CONSOLE', 'Console', " + pastTime + ", 0, 0, 1, 0, 1)",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Wildcard IP mute - should be skipped (unsupported)
      conn.executeStatement(
          "INSERT INTO " + TABLE_PREFIX + "mutes " +
              "(uuid, ip, reason, banned_by_uuid, banned_by_name, time, until, silent, ipban, ipban_wildcard, active) VALUES " +
              "('" + PLAYER_UUID_3 + "', '192.168.0.0/24', 'Wildcard mute - should skip', '" + ACTOR_UUID + "', 'TestActor', " + pastTime + ", 0, 0, 1, 1, 1)",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Warning - player was notified (warned=1)
      conn.executeStatement(
          "INSERT INTO " + TABLE_PREFIX + "warnings " +
              "(uuid, reason, banned_by_uuid, banned_by_name, time, until, warned) VALUES " +
              "('" + PLAYER_UUID_1 + "', 'Test warning', '" + ACTOR_UUID + "', 'TestActor', " + pastTime + ", 0, 1)",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Warning - player was NOT notified (warned=0)
      conn.executeStatement(
          "INSERT INTO " + TABLE_PREFIX + "warnings " +
              "(uuid, reason, banned_by_uuid, banned_by_name, time, until, warned) VALUES " +
              "('" + PLAYER_UUID_2 + "', 'Unread warning', 'CONSOLE', 'Console', " + pastTime + ", 0, 0)",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Kick
      conn.executeStatement(
          "INSERT INTO " + TABLE_PREFIX + "kicks " +
              "(uuid, reason, banned_by_uuid, banned_by_name, time) VALUES " +
              "('" + PLAYER_UUID_1 + "', 'Test kick', '" + ACTOR_UUID + "', 'TestActor', " + pastTime + ")",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // History entry
      conn.executeStatement(
          "INSERT INTO " + TABLE_PREFIX + "history " +
              "(uuid, name, ip, date) VALUES " +
              "('" + PLAYER_UUID_1 + "', 'TestPlayer1', '192.168.1.1', NOW())",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );

      // Entry with invalid UUID (should be skipped)
      conn.executeStatement(
          "INSERT INTO " + TABLE_PREFIX + "bans " +
              "(uuid, ip, reason, banned_by_uuid, banned_by_name, time, until, silent, ipban, ipban_wildcard, active) VALUES " +
              "('#offline#', '192.168.1.99', 'Invalid player', 'CONSOLE', 'Console', " + pastTime + ", 0, 0, 0, 0, 1)",
          DatabaseConnection.DEFAULT_RESULT_FLAGS
      );
    }
  }

  @Test
  public void shouldImportActivePlayerBan() throws SQLException {
    runImport();

    UUID playerUuid = UUID.fromString(PLAYER_UUID_1);
    assertTrue("Player should be banned", plugin.getPlayerBanStorage().isBanned(playerUuid));

    PlayerBanData ban = plugin.getPlayerBanStorage().getBan(playerUuid);
    assertNotNull("Ban data should exist", ban);
    assertEquals("Test ban reason", ban.getReason());
    assertFalse("Ban should not be silent", ban.isSilent());
  }

  @Test
  public void shouldImportActiveIpBan() {
    runImport();

    // Check that IP ban was imported
    // The IP 10.0.0.1 should be banned
    IPAddress ip = new IPAddressString("10.0.0.1").getAddress();
    assertTrue("IP should be banned", plugin.getIpBanStorage().isBanned(ip));
  }

  @Test
  public void shouldImportActiveIpRangeBan() {
    runImport();

    // Check that an IP within the range 172.16.0.0/16 is banned
    IPAddress ip = new IPAddressString("172.16.1.1").getAddress();
    assertTrue("IP in range should be banned", plugin.getIpRangeBanStorage().isBanned(ip));
  }

  @Test
  public void shouldImportActivePlayerMute() throws SQLException {
    runImport();

    UUID playerUuid = UUID.fromString(PLAYER_UUID_1);
    assertTrue("Player should be muted", plugin.getPlayerMuteStorage().isMuted(playerUuid));

    PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(playerUuid);
    assertNotNull("Mute data should exist", mute);
    assertEquals("Test mute reason", mute.getReason());
  }

  @Test
  public void shouldImportActiveIpMute() {
    runImport();

    IPAddress ip = new IPAddressString("10.0.0.2").getAddress();
    assertTrue("IP should be muted", plugin.getIpMuteStorage().isMuted(ip));
  }

  @Test
  public void shouldImportWarningWithCorrectReadStatus() throws SQLException, IOException {
    runImport();

    // Check warnings for PLAYER_UUID_1 - should have warned=true (read=true)
    UUID playerUuid1 = UUID.fromString(PLAYER_UUID_1);
    PlayerData player = plugin.getPlayerStorage().createIfNotExists(playerUuid1, "Unknown");

    try (CloseableIterator<PlayerWarnData> warnings = plugin.getPlayerWarnStorage().getWarnings(player)) {
      boolean foundReadWarning = false;
      while (warnings.hasNext()) {
        PlayerWarnData warn = warnings.next();
        if (warn.getReason().equals("Test warning")) {
          assertTrue("Warning should be marked as read", warn.isRead());
          foundReadWarning = true;
        }
      }
      assertTrue("Should have found the read warning", foundReadWarning);
    }
  }

  @Test
  public void shouldImportKick() throws SQLException {
    runImport();

    UUID playerUuid = UUID.fromString(PLAYER_UUID_1);
    PlayerData player = plugin.getPlayerStorage().createIfNotExists(playerUuid, "Unknown");

    // Verify at least one kick was imported for this player
    long kickCount = plugin.getPlayerKickStorage().getCount(player);
    assertTrue("Should have at least one kick", kickCount > 0);
  }

  @Test
  public void shouldSkipInvalidUuids() {
    // The entry with #offline# UUID should be skipped without error
    runImport();

    // Verify import completed successfully by checking valid entries exist
    assertTrue("Valid player ban should exist",
        plugin.getPlayerBanStorage().isBanned(UUID.fromString(PLAYER_UUID_1)));
  }

  @Test
  public void shouldHandleConsoleAsActor() {
    runImport();

    // The IP ban was created by CONSOLE, verify it imported correctly
    IPAddress ip = new IPAddressString("10.0.0.1").getAddress();
    assertTrue("IP ban by console should exist", plugin.getIpBanStorage().isBanned(ip));

    IpBanData ban = plugin.getIpBanStorage().getBan(ip);
    assertNotNull("Ban should exist", ban);
    assertEquals("Actor should be console",
        plugin.getPlayerStorage().getConsole().getUUID(),
        ban.getActor().getUUID());
  }

  @Test
  public void shouldCreateBanRecordForRemovedBan() throws SQLException {
    runImport();

    // The inactive ban for UUID 550e8400-e29b-41d4-a716-446655440004 should create a record
    UUID removedPlayerUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");

    // Player should NOT be actively banned (it was removed)
    assertFalse("Player should not be actively banned",
        plugin.getPlayerBanStorage().isBanned(removedPlayerUuid));

    // But there should be a ban record
    PlayerData player = plugin.getPlayerStorage().createIfNotExists(removedPlayerUuid, "Unknown");
    long recordCount = plugin.getPlayerBanRecordStorage().getCount(player);
    assertTrue("Should have a ban record", recordCount > 0);
  }

  /**
   * Run the LiteBans import using the plugin's existing connection.
   * This allows tests to work with H2 in-memory databases where
   * separate connections don't share tables.
   */
  private void runImport() {
    // Use the test constructor that accepts an existing connection
    new LiteBans(plugin, plugin.getLocalConn(), TABLE_PREFIX);
  }
}
