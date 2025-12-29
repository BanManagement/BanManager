package me.confuser.banmanager.common.commands.report;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class CloseSubCommandTest extends BasePluginDbTest {

  @Test
  public void shouldQueryReportById() throws SQLException {
    // Test that querying non-existent report returns null
    PlayerReportData report = plugin.getPlayerReportStorage().queryForId(999999);
    assertNull(report);
  }

  @Test
  public void shouldHaveReportStorageAvailable() {
    assertNotNull(plugin.getPlayerReportStorage());
  }
}
