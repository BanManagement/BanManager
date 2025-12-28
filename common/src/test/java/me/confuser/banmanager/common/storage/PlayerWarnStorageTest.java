package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class PlayerWarnStorageTest extends BasePluginDbTest {

  @Test
  public void shouldAddWarning() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    PlayerWarnData warning = new PlayerWarnData(player, actor, "test warning", 1.0);
    assertTrue(plugin.getPlayerWarnStorage().addWarning(warning, false));

    assertTrue(plugin.getPlayerWarnStorage().getCount(player) > 0);
  }

  @Test
  public void shouldGetWarningCount() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    assertEquals(0, plugin.getPlayerWarnStorage().getCount(player));

    plugin.getPlayerWarnStorage().addWarning(new PlayerWarnData(player, actor, "warning 1", 1.0), false);
    assertEquals(1, plugin.getPlayerWarnStorage().getCount(player));

    plugin.getPlayerWarnStorage().addWarning(new PlayerWarnData(player, actor, "warning 2", 1.0), false);
    assertEquals(2, plugin.getPlayerWarnStorage().getCount(player));
  }

  @Test
  public void shouldCalculatePointsTotal() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    // Add warning with 3 points
    PlayerWarnData warning1 = new PlayerWarnData(player, actor, "warning 1", 3.0);
    plugin.getPlayerWarnStorage().addWarning(warning1, false);

    // Add warning with 2 points
    PlayerWarnData warning2 = new PlayerWarnData(player, actor, "warning 2", 2.0);
    plugin.getPlayerWarnStorage().addWarning(warning2, false);

    double totalPoints = plugin.getPlayerWarnStorage().getPointsCount(player);
    assertEquals(5.0, totalPoints, 0.01);
  }

  @Test
  public void shouldGetWarnings() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    plugin.getPlayerWarnStorage().addWarning(new PlayerWarnData(player, actor, "warning 1", 1.0), false);
    plugin.getPlayerWarnStorage().addWarning(new PlayerWarnData(player, actor, "warning 2", 1.0), false);

    CloseableIterator<PlayerWarnData> warnings = plugin.getPlayerWarnStorage().getWarnings(player);
    int count = 0;
    while (warnings.hasNext()) {
      warnings.next();
      count++;
    }

    assertEquals(2, count);
  }

  @Test
  public void shouldDeleteWarning() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    PlayerWarnData warning = new PlayerWarnData(player, actor, "test warning", 1.0);
    plugin.getPlayerWarnStorage().addWarning(warning, false);

    assertEquals(1, plugin.getPlayerWarnStorage().getCount(player));

    plugin.getPlayerWarnStorage().delete(warning);

    assertEquals(0, plugin.getPlayerWarnStorage().getCount(player));
  }

  @Test
  public void shouldHandleTempWarningExpiry() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    // Create a warning that expires in the past
    long expiredTime = (System.currentTimeMillis() / 1000L) - 10;
    PlayerWarnData warning = new PlayerWarnData(player, actor, "temp warning", 1.0, false, expiredTime);

    assertTrue(warning.getExpires() > 0 && warning.getExpires() < System.currentTimeMillis() / 1000L);
  }
}
