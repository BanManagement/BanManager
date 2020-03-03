package me.confuser.banmanager.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class PluginTest extends BasePluginTest {
  @Test
  public void testConfigs() {
    assertTrue(plugin.getConfig().getLocalDb().isEnabled());
    assertEquals("Console", plugin.getConsoleConfig().getName());
  }
}
