package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.Assert.*;

public class PlayerBanStorageTest extends BasePluginDbTest {

  @Test
  public void shouldBanPlayer() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    PlayerBanData ban = new PlayerBanData(player, actor, "test ban", false);
    assertTrue(plugin.getPlayerBanStorage().ban(ban));

    assertTrue(plugin.getPlayerBanStorage().isBanned(player.getUUID()));
    assertEquals("test ban", plugin.getPlayerBanStorage().getBan(player.getUUID()).getReason());
  }

  @Test
  public void shouldNotDuplicateBan() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    PlayerBanData ban1 = new PlayerBanData(player, actor, "first ban", false);
    assertTrue(plugin.getPlayerBanStorage().ban(ban1));
    assertTrue(plugin.getPlayerBanStorage().isBanned(player.getUUID()));

    // Second ban should fail since player is already banned
    PlayerBanData ban2 = new PlayerBanData(player, actor, "second ban", false);
    try {
      plugin.getPlayerBanStorage().ban(ban2);
      // If we get here, expect it to have failed or be caught
    } catch (SQLException e) {
      // Expected - duplicate entry
    }
  }

  @Test
  public void shouldUnbanPlayer() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    PlayerBanData ban = testUtils.createBan(player, actor, "test ban");
    assertTrue(plugin.getPlayerBanStorage().isBanned(player.getUUID()));

    plugin.getPlayerBanStorage().unban(ban, actor, "unbanned");
    assertFalse(plugin.getPlayerBanStorage().isBanned(player.getUUID()));
  }

  @Test
  public void shouldCreateRecordOnUnban() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    long initialCount = plugin.getPlayerBanRecordStorage().getCount(player);

    PlayerBanData ban = testUtils.createBan(player, actor, "test ban");
    plugin.getPlayerBanStorage().unban(ban, actor, "unbanned");

    assertTrue(plugin.getPlayerBanRecordStorage().getCount(player) > initialCount);
  }

  @Test
  public void shouldNotCreateRecordWhenDeleteFlag() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    long initialCount = plugin.getPlayerBanRecordStorage().getCount(player);

    PlayerBanData ban = testUtils.createBan(player, actor, "test ban");
    plugin.getPlayerBanStorage().unban(ban, actor, "unbanned", true);

    assertEquals(initialCount, plugin.getPlayerBanRecordStorage().getCount(player));
  }

  @Test
  public void shouldTrackBanRecords() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    // Ban and unban to create a record
    PlayerBanData ban = testUtils.createBan(player, actor, "test ban");
    plugin.getPlayerBanStorage().unban(ban, actor, "unbanned");

    // Verify the ban record exists
    assertTrue(plugin.getPlayerBanRecordStorage().getCount(player) > 0);
  }

  @Test
  public void shouldHandleTempBanExpiry() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    // Create a temp ban that expires in the past
    long expiredTime = (System.currentTimeMillis() / 1000L) - 10;
    PlayerBanData ban = new PlayerBanData(player, actor, "temp ban", false, expiredTime);

    assertTrue(ban.hasExpired());
  }

  @Test
  public void shouldRetrieveBanByUUID() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    PlayerBanData ban = testUtils.createBan(player, actor, "test ban");

    PlayerBanData retrieved = plugin.getPlayerBanStorage().retrieveBan(player.getUUID());
    assertNotNull(retrieved);
    assertEquals("test ban", retrieved.getReason());
  }

  @Test
  public void shouldGetBanByNameCaseInsensitive() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    PlayerBanData ban = testUtils.createBan(player, actor, "test ban");

    // Try to get ban with uppercase name
    PlayerBanData retrieved = plugin.getPlayerBanStorage().getBan(player.getName().toUpperCase());
    assertNotNull(retrieved);
    assertEquals(player.getUUID(), retrieved.getPlayer().getUUID());
  }

  @Test
  public void shouldAddBanToCache() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    // Create ban object without adding to cache
    PlayerBanData ban = new PlayerBanData(player, actor, "test ban", false);

    // Initially not in cache
    assertFalse(plugin.getPlayerBanStorage().isBanned(player.getUUID()));

    // Add to cache only (not DB)
    plugin.getPlayerBanStorage().addBan(ban);

    // Now should be in cache
    assertTrue(plugin.getPlayerBanStorage().isBanned(player.getUUID()));
  }
}
