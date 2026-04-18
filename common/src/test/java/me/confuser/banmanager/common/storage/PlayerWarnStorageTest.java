package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  public void shouldMarkAllRead() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    PlayerWarnData w1 = new PlayerWarnData(player, actor, "unread 1", 1.0, false);
    plugin.getPlayerWarnStorage().addWarning(w1, false);
    PlayerWarnData w2 = new PlayerWarnData(player, actor, "unread 2", 1.0, false);
    plugin.getPlayerWarnStorage().addWarning(w2, false);

    long unreadBefore = plugin.getPlayerWarnStorage().queryBuilder()
        .where().eq("player_id", player.getId()).and().eq("read", false)
        .countOf();
    assertEquals(2, unreadBefore);

    int updated = plugin.getPlayerWarnStorage().markAllRead(player.getUUID());
    assertEquals(2, updated);

    long unreadAfter = plugin.getPlayerWarnStorage().queryBuilder()
        .where().eq("player_id", player.getId()).and().eq("read", false)
        .countOf();
    assertEquals(0, unreadAfter);
  }

  @Test
  public void shouldNotMarkOtherPlayersRead() throws SQLException {
    PlayerData player1 = testUtils.createRandomPlayer();
    PlayerData player2 = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();

    plugin.getPlayerWarnStorage().addWarning(new PlayerWarnData(player1, actor, "p1 warn", 1.0, false), false);
    plugin.getPlayerWarnStorage().addWarning(new PlayerWarnData(player2, actor, "p2 warn", 1.0, false), false);

    plugin.getPlayerWarnStorage().markAllRead(player1.getUUID());

    long p1Unread = plugin.getPlayerWarnStorage().queryBuilder()
        .where().eq("player_id", player1.getId()).and().eq("read", false)
        .countOf();
    assertEquals(0, p1Unread);

    long p2Unread = plugin.getPlayerWarnStorage().queryBuilder()
        .where().eq("player_id", player2.getId()).and().eq("read", false)
        .countOf();
    assertEquals(1, p2Unread);
  }
}
