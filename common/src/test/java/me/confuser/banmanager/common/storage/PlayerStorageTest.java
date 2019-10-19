package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlayerStorageTest extends BasePluginDbTest {

  @Test
  public void shouldCreateConsole() {
    PlayerData data = plugin.getPlayerStorage().getConsole();

    assertEquals("Console", data.getName());
    assertEquals(plugin.getConsoleConfig().getUuid(), data.getUUID());
  }
}
