package me.confuser.banmanager.common.runnables;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.RollbackData;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class RollbackSyncTest extends BasePluginDbTest {
  private RollbackSync rollbackSync;

  @Before
  public void setupSync() {
    rollbackSync = new RollbackSync(plugin);
  }

  @Test
  public void shouldRemoveBansFromCache() throws SQLException {
    // Setup: Create a player and ban them
    PlayerData victim = testUtils.createRandomPlayer();
    PlayerData maliciousMod = testUtils.createRandomPlayer();

    PlayerBanData ban = testUtils.createBan(victim, maliciousMod, "test ban");
    assertTrue(plugin.getPlayerBanStorage().isBanned(victim.getUUID()));

    long now = System.currentTimeMillis() / 1000L;
    long oneHourAgo = now - 3600;

    // Create a rollback entry for "bans" type
    RollbackData rollbackData = new RollbackData(maliciousMod, maliciousMod, "bans", oneHourAgo, now);
    plugin.getRollbackStorage().create(rollbackData);

    // Run the sync
    rollbackSync.run();

    // Verify ban was removed from cache
    assertFalse(plugin.getPlayerBanStorage().isBanned(victim.getUUID()));
  }

  @Test
  public void shouldAddRestoredBansToCache() throws SQLException {
    // Setup: Create a ban directly in the database (simulating restored ban)
    PlayerData victim = testUtils.createRandomPlayer();
    PlayerData legitMod = testUtils.createRandomPlayer();
    PlayerData maliciousMod = testUtils.createRandomPlayer();

    // Create ban directly in database without adding to cache
    PlayerBanData ban = new PlayerBanData(victim, legitMod, "restored ban", false);
    plugin.getPlayerBanStorage().create(ban);

    // Verify ban is in DB but not in cache
    assertNotNull(plugin.getPlayerBanStorage().retrieveBan(victim.getUUID()));
    assertFalse(plugin.getPlayerBanStorage().isBanned(victim.getUUID()));

    long now = System.currentTimeMillis() / 1000L;
    long oneHourAgo = now - 3600;

    // Create a rollback entry for "banrecords" type
    RollbackData rollbackData = new RollbackData(maliciousMod, maliciousMod, "banrecords", oneHourAgo, now);
    plugin.getRollbackStorage().create(rollbackData);

    // Run the sync
    rollbackSync.run();

    // Verify ban was added to cache
    assertTrue(plugin.getPlayerBanStorage().isBanned(victim.getUUID()));
    assertEquals("restored ban", plugin.getPlayerBanStorage().getBan(victim.getUUID()).getReason());
  }

  @Test
  public void shouldRemoveMutesFromCache() throws SQLException {
    // Setup: Create a player and mute them
    PlayerData victim = testUtils.createRandomPlayer();
    PlayerData maliciousMod = testUtils.createRandomPlayer();

    PlayerMuteData mute = testUtils.createMute(victim, maliciousMod, "test mute");
    assertTrue(plugin.getPlayerMuteStorage().isMuted(victim.getUUID()));

    long now = System.currentTimeMillis() / 1000L;
    long oneHourAgo = now - 3600;

    // Create a rollback entry for "mutes" type
    RollbackData rollbackData = new RollbackData(maliciousMod, maliciousMod, "mutes", oneHourAgo, now);
    plugin.getRollbackStorage().create(rollbackData);

    // Run the sync
    rollbackSync.run();

    // Verify mute was removed from cache
    assertFalse(plugin.getPlayerMuteStorage().isMuted(victim.getUUID()));
  }

  @Test
  public void shouldAddRestoredMutesToCache() throws SQLException {
    // Setup: Create a mute directly in the database (simulating restored mute)
    PlayerData victim = testUtils.createRandomPlayer();
    PlayerData legitMod = testUtils.createRandomPlayer();
    PlayerData maliciousMod = testUtils.createRandomPlayer();

    // Create mute directly in database without adding to cache
    PlayerMuteData mute = new PlayerMuteData(victim, legitMod, "restored mute", false, false);
    plugin.getPlayerMuteStorage().create(mute);

    // Verify mute is in DB but not in cache
    assertNotNull(plugin.getPlayerMuteStorage().retrieveMute(victim.getUUID()));
    assertFalse(plugin.getPlayerMuteStorage().isMuted(victim.getUUID()));

    long now = System.currentTimeMillis() / 1000L;
    long oneHourAgo = now - 3600;

    // Create a rollback entry for "muterecords" type
    RollbackData rollbackData = new RollbackData(maliciousMod, maliciousMod, "muterecords", oneHourAgo, now);
    plugin.getRollbackStorage().create(rollbackData);

    // Run the sync
    rollbackSync.run();

    // Verify mute was added to cache
    assertTrue(plugin.getPlayerMuteStorage().isMuted(victim.getUUID()));
    assertEquals("restored mute", plugin.getPlayerMuteStorage().getMute(victim.getUUID()).getReason());
  }

  @Test
  public void shouldNotRemoveBansOutsideTimeframe() throws SQLException {
    // Setup: Create a ban with a very old timestamp
    PlayerData victim = testUtils.createRandomPlayer();
    PlayerData maliciousMod = testUtils.createRandomPlayer();

    // Create a ban with a timestamp in the past (simulating old ban)
    long oldTimestamp = (System.currentTimeMillis() / 1000L) - 86400; // 1 day ago
    PlayerBanData ban = new PlayerBanData(victim, maliciousMod, "old ban", false, 0, oldTimestamp);
    plugin.getPlayerBanStorage().create(ban);
    plugin.getPlayerBanStorage().addBan(ban);

    assertTrue(plugin.getPlayerBanStorage().isBanned(victim.getUUID()));

    long now = System.currentTimeMillis() / 1000L;
    long oneHourAgo = now - 3600; // Rollback only last hour

    // Create a rollback entry that only covers the last hour
    RollbackData rollbackData = new RollbackData(maliciousMod, maliciousMod, "bans", oneHourAgo, now);
    plugin.getRollbackStorage().create(rollbackData);

    // Run the sync
    rollbackSync.run();

    // Verify ban was NOT removed (since it was created outside the timeframe)
    assertTrue(plugin.getPlayerBanStorage().isBanned(victim.getUUID()));
  }

  @Test
  public void shouldOnlyRemoveBansBySpecificActor() throws SQLException {
    // Setup: Create bans by different actors
    PlayerData victim1 = testUtils.createRandomPlayer();
    PlayerData victim2 = testUtils.createRandomPlayer();
    PlayerData maliciousMod = testUtils.createRandomPlayer();
    PlayerData legitMod = testUtils.createRandomPlayer();

    // Malicious mod bans victim1
    PlayerBanData ban1 = testUtils.createBan(victim1, maliciousMod, "malicious ban");
    // Legit mod bans victim2
    PlayerBanData ban2 = testUtils.createBan(victim2, legitMod, "legit ban");

    assertTrue(plugin.getPlayerBanStorage().isBanned(victim1.getUUID()));
    assertTrue(plugin.getPlayerBanStorage().isBanned(victim2.getUUID()));

    long now = System.currentTimeMillis() / 1000L;
    long oneHourAgo = now - 3600;

    // Create a rollback entry only for malicious mod
    RollbackData rollbackData = new RollbackData(maliciousMod, maliciousMod, "bans", oneHourAgo, now);
    plugin.getRollbackStorage().create(rollbackData);

    // Run the sync
    rollbackSync.run();

    // Verify only malicious mod's ban was removed
    assertFalse(plugin.getPlayerBanStorage().isBanned(victim1.getUUID()));
    // Legit mod's ban should still be in cache
    assertTrue(plugin.getPlayerBanStorage().isBanned(victim2.getUUID()));
  }
}
