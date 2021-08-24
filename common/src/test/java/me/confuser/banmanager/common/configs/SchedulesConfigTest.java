package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.BasePluginTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SchedulesConfigTest extends BasePluginTest {
  @Test
  public void isValid() {
    SchedulesConfig config = plugin.getSchedulesConfig();

    assertEquals(30, config.getSchedule("expiresCheck"));
    assertEquals(30, config.getSchedule("playerBans"));
    assertEquals(30, config.getSchedule("playerMutes"));
    assertEquals(30, config.getSchedule("playerWarnings"));
    assertEquals(30, config.getSchedule("ipBans"));
    assertEquals(30, config.getSchedule("ipRangeBans"));
    assertEquals(30, config.getSchedule("rollbacks"));
    assertEquals(30, config.getSchedule("nameBans"));
    assertEquals(120, config.getSchedule("externalPlayerBans"));
    assertEquals(120, config.getSchedule("externalPlayerMutes"));
    assertEquals(120, config.getSchedule("externalPlayerNotes"));
    assertEquals(120, config.getSchedule("externalIpBans"));
    assertEquals(60, config.getSchedule("saveLastChecked"));

    assertEquals(0, config.getLastChecked("expiresCheck"));
    assertEquals(0, config.getLastChecked("playerBans"));
    assertEquals(0, config.getLastChecked("playerMutes"));
    assertEquals(0, config.getLastChecked("playerWarnings"));
    assertEquals(0, config.getLastChecked("ipBans"));
    assertEquals(0, config.getLastChecked("ipRangeBans"));
    assertEquals(0, config.getLastChecked("rollbacks"));
    assertEquals(0, config.getLastChecked("nameBans"));
    assertEquals(0, config.getLastChecked("externalPlayerBans"));
    assertEquals(0, config.getLastChecked("externalPlayerMutes"));
    assertEquals(0, config.getLastChecked("externalPlayerNotes"));
    assertEquals(0, config.getLastChecked("externalIpBans"));
  }
}
