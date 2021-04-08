package me.confuser.banmanager.common.storage;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.Assert.*;

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
    CreateOrUpdateStatus status = playerStorage.upsert(data);
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
  public void shouldRetrievePlayerData() throws SQLException {
    PlayerStorage playerStorage = plugin.getPlayerStorage();

    UUID uuid = UUID.randomUUID();

    PlayerData data = new PlayerData(uuid, "PlaYer 1");
    CreateOrUpdateStatus status = playerStorage.upsert(data);
    assertTrue(status.isCreated());
    assertFalse(status.isUpdated());

    data = playerStorage.retrieve("player 1", false);

    assertEquals(data.getUUID(), uuid);
    assertEquals(data.getName(), "PlaYer 1");
  }
}
