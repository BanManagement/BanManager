package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerWarnData;
import me.confuser.banmanager.events.PlayerWarnEvent;
import org.bukkit.Bukkit;

import java.sql.SQLException;

public class PlayerWarnStorage extends BaseDaoImpl<PlayerWarnData, Integer> {

  private BanManager plugin = BanManager.getPlugin();

  public PlayerWarnStorage(ConnectionSource connection, DatabaseTableConfig<PlayerWarnData> tableConfig) throws SQLException {
    super(connection, tableConfig);
  }

  public boolean addWarning(PlayerWarnData data) throws SQLException {
    PlayerWarnEvent event = new PlayerWarnEvent(data);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    return create(data) == 1;
  }

  public CloseableIterator<PlayerWarnData> getUnreadWarnings(PlayerData player) throws SQLException {
    return queryBuilder().where().eq("player_id", player).and().eq("read", false).iterator();
  }

  public CloseableIterator<PlayerWarnData> getWarnings(PlayerData player) throws SQLException {
    return queryBuilder().where().eq("player_id", player).iterator();
  }

  public long getCount(PlayerData player) throws SQLException {
    return queryBuilder().where().eq("player_id", player).countOf();
  }

  public boolean isRecentlyWarned(PlayerData player) throws SQLException {
    if (plugin.getConfiguration().getWarningCooldown() == 0) {
      return false;
    }

    return queryBuilder().where()
                         .eq("player_id", player).and()
                         .ge("created", (System.currentTimeMillis() / 1000L) - plugin.getConfiguration()
                                                                                     .getWarningCooldown())
                         .countOf() > 0;
  }

  public int deleteRecent(PlayerData player) throws SQLException {
    // TODO use a raw DELETE query to reduce to one query
    PlayerWarnData warning = queryBuilder().limit(1L).orderBy("created", false).where().eq("player_id", player)
                                           .queryForFirst();

    return delete(warning);
  }
}
