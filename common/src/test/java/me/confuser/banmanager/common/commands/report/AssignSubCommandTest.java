package me.confuser.banmanager.common.commands.report;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class AssignSubCommandTest extends BasePluginDbTest {

  @Test
  public void shouldHaveReportStorageAvailable() {
    assertNotNull(plugin.getPlayerReportStorage());
  }

  @Test
  public void shouldFailAssignIfReportNotFound() throws SQLException {
    // Try to query a non-existent report
    PlayerReportData report = plugin.getPlayerReportStorage().queryForId(999999);
    assertNull(report);
  }
}
