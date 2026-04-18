package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ormlite.dao.Dao;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerStorageTest extends BasePluginDbTest {

  @Test
  public void shouldCreateConsole() {
    PlayerData data = plugin.getPlayerStorage().getConsole();

    assertEquals("Console", data.getName());
    assertEquals(plugin.getConsoleConfig().getUuid(), data.getUUID());
  }

  @Test
  public void shouldUpsertPlayerData() throws SQLException {
    PlayerStorage playerStorage = plugin.getPlayerStorage();

    UUID uuid = UUID.randomUUID();

    PlayerData data = new PlayerData(uuid, "Name 1");
    Dao.CreateOrUpdateStatus status = playerStorage.upsert(data);
    assertTrue(status.isCreated());
    assertFalse(status.isUpdated());
    assertNotNull(playerStorage.getAutoCompleteTree().getValueForExactKey("Name 1"));

    data = new PlayerData(uuid, "Name 2");
    status = playerStorage.upsert(data);
    assertFalse(status.isCreated());
    assertTrue(status.isUpdated());
    assertNull(playerStorage.getAutoCompleteTree().getValueForExactKey("Name 1"));
    assertNotNull(playerStorage.getAutoCompleteTree().getValueForExactKey("Name 2"));
  }

  @Test
  public void shouldPersistLocale() throws SQLException {
    PlayerStorage playerStorage = plugin.getPlayerStorage();

    UUID uuid = UUID.randomUUID();
    PlayerData data = new PlayerData(uuid, "LocalePlayer");
    data.setLocale("de");

    playerStorage.upsert(data);

    PlayerData retrieved = playerStorage.retrieve("LocalePlayer", false);
    assertNotNull(retrieved);
    assertEquals("de", retrieved.getLocale());
  }

  @Test
  public void shouldUpdateLocale() throws SQLException {
    PlayerStorage playerStorage = plugin.getPlayerStorage();

    UUID uuid = UUID.randomUUID();
    PlayerData data = new PlayerData(uuid, "LocaleUpdate");
    data.setLocale("en");
    playerStorage.upsert(data);

    data.setLocale("fr");
    playerStorage.upsert(data);

    PlayerData retrieved = playerStorage.retrieve("LocaleUpdate", false);
    assertNotNull(retrieved);
    assertEquals("fr", retrieved.getLocale());
  }

  @Test
  public void shouldHandleNullLocale() throws SQLException {
    PlayerStorage playerStorage = plugin.getPlayerStorage();

    UUID uuid = UUID.randomUUID();
    PlayerData data = new PlayerData(uuid, "NullLocale");
    playerStorage.upsert(data);

    PlayerData retrieved = playerStorage.retrieve("NullLocale", false);
    assertNotNull(retrieved);
    assertNull(retrieved.getLocale());
  }

  @Test
  public void shouldRetrievePlayerData() throws SQLException {
    PlayerStorage playerStorage = plugin.getPlayerStorage();

    UUID uuid = UUID.randomUUID();

    PlayerData data = new PlayerData(uuid, "PlaYer 1");
    Dao.CreateOrUpdateStatus status = playerStorage.upsert(data);
    assertTrue(status.isCreated());
    assertFalse(status.isUpdated());

    data = playerStorage.retrieve("player 1", false);

    assertEquals(data.getUUID(), uuid);
    assertEquals(data.getName(), "PlaYer 1");
  }
}
