package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.configs.CleanUp;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerHistoryData;
import me.confuser.banmanager.common.data.PlayerNameSummary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerHistoryStorageTest extends BasePluginDbTest {
  @AfterEach
  public void clear() throws SQLException {
    plugin.getPlayerHistoryStorage().updateRaw("TRUNCATE TABLE " + plugin.getPlayerHistoryStorage().getTableName());
  }

  @Test
  public void shouldNotPurgeWhenDaysIsZero() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerHistoryStorage storage = plugin.getPlayerHistoryStorage();

    testUtils.createSession(player, true);
    testUtils.createSession(player, true);

    assertEquals(2, storage.countOf());

    storage.purge(new CleanUp(0));

    assertEquals(2, storage.countOf());
  }

  @Test
  public void shouldPurgeOlderThan30Days() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerHistoryStorage storage = plugin.getPlayerHistoryStorage();
    String table = storage.getTableName();
    long oldLeaveTime = (System.currentTimeMillis() / 1000L) - (86400 * 31);

    // Create first session and set old leave time
    testUtils.createSession(player, true);
    storage.updateRaw("UPDATE `" + table + "` SET `leave` = " + oldLeaveTime + " WHERE `id` = (SELECT MAX(id) FROM (SELECT id FROM `" + table + "`) AS t)");

    // Create second session with current time
    testUtils.createSession(player, true);

    assertEquals(2, storage.countOf());

    storage.purge(new CleanUp(30));

    assertEquals(1, storage.countOf());
  }

  @Test
  public void shouldNotPurgeIfBanned() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerHistoryStorage storage = plugin.getPlayerHistoryStorage();
    String table = storage.getTableName();
    IpBanData banData = new IpBanData(player.getIp(), player, "test", false);
    long oldLeaveTime = (System.currentTimeMillis() / 1000L) - (86401 * 30);

    // Create session and set old leave time
    testUtils.createSession(player, true);
    storage.updateRaw("UPDATE `" + table + "` SET `leave` = " + oldLeaveTime);

    assertEquals(1, storage.countOf());
    plugin.getIpBanStorage().create(banData);

    storage.purge(new CleanUp(30));

    assertEquals(1, storage.countOf());
    plugin.getIpBanStorage().delete(banData);
  }

  @Test
  public void shouldCreateSessionWithNullIp() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerHistoryStorage storage = plugin.getPlayerHistoryStorage();

    testUtils.createSession(player, false);

    assertEquals(1, storage.countOf());

    // Fetch from DB to verify
    List<PlayerHistoryData> sessions = storage.queryForAll();
    assertEquals(1, sessions.size());
    assertNull(sessions.get(0).getIp(), "IP should be null when logIp=false");
    assertEquals(player.getName(), sessions.get(0).getName());
  }

  @Test
  public void shouldGetNamesSummary() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerHistoryStorage storage = plugin.getPlayerHistoryStorage();

    testUtils.createSession(player, true);

    List<PlayerNameSummary> summary = storage.getNamesSummary(player);
    assertEquals(1, summary.size());
    assertEquals(player.getName(), summary.get(0).name());
  }

  @Test
  public void shouldGetNameAt() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerHistoryStorage storage = plugin.getPlayerHistoryStorage();

    testUtils.createSession(player, true);

    // Fetch the session to get its timestamps
    List<PlayerHistoryData> sessions = storage.queryForAll();
    assertEquals(1, sessions.size());
    PlayerHistoryData session = sessions.get(0);

    String nameAt = storage.getNameAt(player, session.getJoin());
    assertEquals(player.getName(), nameAt);
  }

  @Test
  public void shouldPurgeSessionsWithNullIp() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerHistoryStorage storage = plugin.getPlayerHistoryStorage();
    String table = storage.getTableName();
    long oldLeaveTime = (System.currentTimeMillis() / 1000L) - (86400 * 31);

    testUtils.createSession(player, false);
    storage.updateRaw("UPDATE `" + table + "` SET `leave` = " + oldLeaveTime);

    assertEquals(1, storage.countOf());

    storage.purge(new CleanUp(30));

    assertEquals(0, storage.countOf());
  }

  @Test
  public void shouldSetDatabaseTimestampsOnSession() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerHistoryStorage storage = plugin.getPlayerHistoryStorage();

    testUtils.startSession(player, true);

    // Session should be in memory
    assertTrue(storage.hasActiveSession(player.getUUID()));

    // And persisted in DB
    assertEquals(1, storage.countOf());

    // End the session
    testUtils.endSession(player);

    // Session should no longer be in memory
    assertFalse(storage.hasActiveSession(player.getUUID()));

    // Verify timestamps are set and leave > join (or equal for very fast tests)
    List<PlayerHistoryData> sessions = storage.queryForAll();
    assertEquals(1, sessions.size());
    PlayerHistoryData session = sessions.get(0);

    assertTrue(session.getJoin() > 0, "Join time should be set");
    assertTrue(session.getLeave() > 0, "Leave time should be set");
    assertTrue(session.getLeave() >= session.getJoin(), "Leave should be >= Join");
  }
}
