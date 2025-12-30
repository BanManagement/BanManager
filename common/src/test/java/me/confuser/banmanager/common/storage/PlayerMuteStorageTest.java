package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerMuteRecord;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class PlayerMuteStorageTest extends BasePluginDbTest {

  @Test
  public void shouldMutePlayer() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    PlayerMuteData mute = new PlayerMuteData(player, actor, "test mute", false, false);
    assertTrue(plugin.getPlayerMuteStorage().mute(mute));

    assertTrue(plugin.getPlayerMuteStorage().isMuted(player.getUUID()));
    assertEquals("test mute", plugin.getPlayerMuteStorage().getMute(player.getUUID()).getReason());
  }

  @Test
  public void shouldNotDuplicateMute() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    PlayerMuteData mute1 = new PlayerMuteData(player, actor, "first mute", false, false);
    assertTrue(plugin.getPlayerMuteStorage().mute(mute1));
    assertTrue(plugin.getPlayerMuteStorage().isMuted(player.getUUID()));

    // Second mute should fail since player is already muted
    PlayerMuteData mute2 = new PlayerMuteData(player, actor, "second mute", false, false);
    try {
      plugin.getPlayerMuteStorage().mute(mute2);
    } catch (SQLException e) {
      // Expected - duplicate entry
    }
  }

  @Test
  public void shouldUnmutePlayer() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    PlayerMuteData mute = testUtils.createMute(player, actor, "test mute");
    assertTrue(plugin.getPlayerMuteStorage().isMuted(player.getUUID()));

    plugin.getPlayerMuteStorage().unmute(mute, actor, "unmuted");
    assertFalse(plugin.getPlayerMuteStorage().isMuted(player.getUUID()));
  }

  @Test
  public void shouldCreateRecordOnUnmute() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    long initialCount = plugin.getPlayerMuteRecordStorage().getCount(player);

    PlayerMuteData mute = testUtils.createMute(player, actor, "test mute");
    plugin.getPlayerMuteStorage().unmute(mute, actor, "unmuted");

    assertTrue(plugin.getPlayerMuteRecordStorage().getCount(player) > initialCount);
  }

  @Test
  public void shouldNotCreateRecordWhenDeleteFlag() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    long initialCount = plugin.getPlayerMuteRecordStorage().getCount(player);

    PlayerMuteData mute = testUtils.createMute(player, actor, "test mute");
    plugin.getPlayerMuteStorage().unmute(mute, actor, "unmuted", true);

    assertEquals(initialCount, plugin.getPlayerMuteRecordStorage().getCount(player));
  }

  @Test
  public void shouldTrackMuteRecords() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    // Mute and unmute to create a record
    PlayerMuteData mute = testUtils.createMute(player, actor, "test mute");
    plugin.getPlayerMuteStorage().unmute(mute, actor, "unmuted");

    // Verify the mute record exists
    assertTrue(plugin.getPlayerMuteRecordStorage().getCount(player) > 0);
  }

  @Test
  public void shouldHandleTempMuteExpiry() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    // Create a temp mute that expires in the past
    long expiredTime = (System.currentTimeMillis() / 1000L) - 10;
    PlayerMuteData mute = new PlayerMuteData(player, actor, "temp mute", false, false, expiredTime);

    assertTrue(mute.hasExpired());
  }

  @Test
  public void shouldHandleSoftMute() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    PlayerMuteData mute = new PlayerMuteData(player, actor, "soft mute", false, true);
    assertTrue(plugin.getPlayerMuteStorage().mute(mute));

    PlayerMuteData retrieved = plugin.getPlayerMuteStorage().getMute(player.getUUID());
    assertNotNull(retrieved);
    assertTrue(retrieved.isSoft());
  }

  @Test
  public void shouldCreateOnlineOnlyMute() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    long expires = (System.currentTimeMillis() / 1000L) + 86400; // 1 day from now
    PlayerMuteData mute = new PlayerMuteData(player, actor, "online only mute", false, false, expires, true);
    assertTrue(plugin.getPlayerMuteStorage().mute(mute));

    PlayerMuteData retrieved = plugin.getPlayerMuteStorage().getMute(player.getUUID());
    assertNotNull(retrieved);
    assertTrue(retrieved.isOnlineOnly());
    assertFalse(retrieved.isPaused());
    assertEquals(expires, retrieved.getExpires());
  }

  @Test
  public void shouldCreatePausedOnlineOnlyMute() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    // Create mute for offline player (starts paused)
    PlayerMuteData mute = new PlayerMuteData(player, actor, "paused online mute", false, false, 0, true);
    mute.setPausedRemaining(86400); // 1 day remaining
    assertTrue(plugin.getPlayerMuteStorage().mute(mute));

    PlayerMuteData retrieved = plugin.getPlayerMuteStorage().getMute(player.getUUID());
    assertNotNull(retrieved);
    assertTrue(retrieved.isOnlineOnly());
    assertTrue(retrieved.isPaused());
    assertEquals(0, retrieved.getExpires());
    assertEquals(86400, retrieved.getPausedRemaining());
  }

  @Test
  public void shouldPauseMute() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    long expires = (System.currentTimeMillis() / 1000L) + 86400;
    PlayerMuteData mute = new PlayerMuteData(player, actor, "online only mute", false, false, expires, true);
    assertTrue(plugin.getPlayerMuteStorage().mute(mute));

    // Pause the mute
    long remaining = 43200; // 12 hours remaining
    assertTrue(plugin.getPlayerMuteStorage().pauseMute(mute, remaining));

    PlayerMuteData retrieved = plugin.getPlayerMuteStorage().getMute(player.getUUID());
    assertTrue(retrieved.isPaused());
    assertEquals(0, retrieved.getExpires());
    assertEquals(remaining, retrieved.getPausedRemaining());
  }

  @Test
  public void shouldResumeMute() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    // Create paused mute
    PlayerMuteData mute = new PlayerMuteData(player, actor, "paused mute", false, false, 0, true);
    mute.setPausedRemaining(43200); // 12 hours remaining
    assertTrue(plugin.getPlayerMuteStorage().mute(mute));

    // Resume the mute
    assertTrue(plugin.getPlayerMuteStorage().resumeMute(mute));

    PlayerMuteData retrieved = plugin.getPlayerMuteStorage().getMute(player.getUUID());
    assertFalse(retrieved.isPaused());
    assertEquals(0, retrieved.getPausedRemaining());
    assertTrue(retrieved.getExpires() > (System.currentTimeMillis() / 1000L));
  }

  @Test
  public void shouldNotPauseAlreadyPausedMute() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    // Create already paused mute
    PlayerMuteData mute = new PlayerMuteData(player, actor, "paused mute", false, false, 0, true);
    mute.setPausedRemaining(86400);
    assertTrue(plugin.getPlayerMuteStorage().mute(mute));

    // Try to pause again - should fail due to conditional update
    assertFalse(plugin.getPlayerMuteStorage().pauseMute(mute, 43200));
  }

  @Test
  public void shouldNotResumeActiveMute() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    long expires = (System.currentTimeMillis() / 1000L) + 86400;
    PlayerMuteData mute = new PlayerMuteData(player, actor, "active mute", false, false, expires, true);
    assertTrue(plugin.getPlayerMuteStorage().mute(mute));

    // Try to resume an active mute - should fail due to conditional update
    assertFalse(plugin.getPlayerMuteStorage().resumeMute(mute));
  }

  @Test
  public void shouldNotConsiderPausedMuteExpired() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    // Create paused mute with expires=0
    PlayerMuteData mute = new PlayerMuteData(player, actor, "paused mute", false, false, 0, true);
    mute.setPausedRemaining(86400);

    // Paused mute should not be considered expired
    assertFalse(mute.hasExpired());
  }

  @Test
  public void shouldNotRestoreOnlineOnlyWhenNoRemainingTime() throws SQLException, IOException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();
    PlayerData unmuter = testUtils.createRandomPlayer();

    // Create an online-only mute that has already expired (expires in the past)
    long expiredTime = (System.currentTimeMillis() / 1000L) - 100; // 100 seconds ago
    PlayerMuteData mute = new PlayerMuteData(player, actor, "expired online mute", false, false, expiredTime, true);
    assertTrue(plugin.getPlayerMuteStorage().mute(mute));

    // Unmute to create a record - this simulates what ExpiresSync does
    // At this point, remainingOnlineTime will be 0 because the mute has expired
    plugin.getPlayerMuteStorage().unmute(mute, unmuter, "expired");

    // Get the record
    CloseableIterator<PlayerMuteRecord> iterator = plugin.getPlayerMuteRecordStorage().getRecords(player);
    assertTrue(iterator.hasNext());
    PlayerMuteRecord record = iterator.next();
    iterator.close();

    // Verify the record has onlineOnly=true but remainingOnlineTime=0
    assertTrue(record.isOnlineOnly());
    assertEquals(0, record.getRemainingOnlineTime());

    // Create a new mute from the record (simulating rollback)
    PlayerMuteData rolledBack = new PlayerMuteData(record);

    // The rolled back mute should NOT be marked as onlineOnly since there's no remaining time
    // This prevents the inconsistent state where onlineOnly=true but expires is in the past
    assertFalse(rolledBack.isOnlineOnly());
    assertEquals(0, rolledBack.getPausedRemaining());
    assertEquals(expiredTime, rolledBack.getExpires());
  }

  @Test
  public void shouldRestoreOnlineOnlyWithRemainingTime() throws SQLException, IOException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();
    PlayerData unmuter = testUtils.createRandomPlayer();

    // Create an active online-only mute
    long expires = (System.currentTimeMillis() / 1000L) + 86400; // 1 day from now
    PlayerMuteData mute = new PlayerMuteData(player, actor, "active online mute", false, false, expires, true);
    assertTrue(plugin.getPlayerMuteStorage().mute(mute));

    // Unmute while there's still remaining time
    plugin.getPlayerMuteStorage().unmute(mute, unmuter, "unmuted early");

    // Get the record
    CloseableIterator<PlayerMuteRecord> iterator = plugin.getPlayerMuteRecordStorage().getRecords(player);
    assertTrue(iterator.hasNext());
    PlayerMuteRecord record = iterator.next();
    iterator.close();

    // Verify the record has remaining time
    assertTrue(record.isOnlineOnly());
    assertTrue(record.getRemainingOnlineTime() > 0);

    // Create a new mute from the record (simulating rollback)
    PlayerMuteData rolledBack = new PlayerMuteData(record);

    // The rolled back mute should be marked as onlineOnly and paused
    assertTrue(rolledBack.isOnlineOnly());
    assertTrue(rolledBack.isPaused());
    assertEquals(0, rolledBack.getExpires());
    assertTrue(rolledBack.getPausedRemaining() > 0);
  }
}
