package me.confuser.banmanager.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class PluginTest extends BasePluginTest {
  @Test
  public void testConfigs() {
    assertEquals(10, plugin.getConfig().getLocalDb().getMaxConnections());
    assertEquals("Console", plugin.getConsoleConfig().getName());
  }
}
