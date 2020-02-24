package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.configs.CleanUp;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerHistoryData;
import org.junit.After;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class PlayerHistoryStorageTest extends BasePluginDbTest {
  @After
  public void clear() throws SQLException {
    plugin.getPlayerHistoryStorage().updateRaw("TRUNCATE TABLE " + plugin.getPlayerHistoryStorage().getTableName());
  }

  @Test
  public void shouldNotPurge() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerHistoryStorage storage = plugin.getPlayerHistoryStorage();
    storage.create(new PlayerHistoryData(player));
    storage.create(new PlayerHistoryData(player));

    assertEquals(2, storage.countOf());

    storage.purge(new CleanUp(0));

    assertEquals(2, storage.countOf());
  }

  @Test
  public void shouldPurgeOlderThan30Days() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerHistoryStorage storage = plugin.getPlayerHistoryStorage();
    PlayerHistoryData data = new PlayerHistoryData(player);
    long leaveTime = (System.currentTimeMillis() / 1000L) - (86400 * 31);

    storage.create(data);

    data.setLeave(leaveTime);
    storage.create(data);

    assertEquals(2, storage.countOf());

    storage.purge(new CleanUp(30));

    assertEquals(1, storage.countOf());
  }

  @Test
  public void shouldNotPurgeIfBanned() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerHistoryStorage storage = plugin.getPlayerHistoryStorage();
    PlayerHistoryData data = new PlayerHistoryData(player);
    IpBanData banData = new IpBanData(player.getIp(), player, "test", false);
    long leaveTime = (System.currentTimeMillis() / 1000L) - (86401 * 30);

    data.setLeave(leaveTime);
    storage.create(data);

    assertEquals(1, storage.countOf());
    plugin.getIpBanStorage().create(banData);

    storage.purge(new CleanUp(30));

    assertEquals(1, storage.countOf());
    plugin.getIpBanStorage().delete(banData);
  }
}
