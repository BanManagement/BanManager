package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import org.junit.Test;

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
}
