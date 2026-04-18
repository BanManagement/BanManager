package me.confuser.banmanager.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PluginTest extends BasePluginTest {
  @Test
  public void testConfigs() {
    assertEquals(10, plugin.getConfig().getLocalDb().getMaxConnections());
    assertEquals("Console", plugin.getConsoleConfig().getName());
  }
}
