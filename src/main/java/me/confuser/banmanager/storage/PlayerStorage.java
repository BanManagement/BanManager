package me.confuser.banmanager.storage;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.SmartArrayBasedNodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.voidvalue.VoidValue;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.UUIDProfile;
import me.confuser.banmanager.util.UUIDUtils;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PlayerStorage extends BaseDaoImpl<PlayerData, byte[]> {

  private BanManager plugin = BanManager.getPlugin();
  @Getter
  private RadixTree<VoidValue> autoCompleteTree;

  @Getter
  private PlayerData console;

  public PlayerStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerData>) BanManager.getPlugin().getConfiguration().getLocalDb()
                                                                  .getTable("players"));

    if (!isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }

    setupConsole();
    if (plugin.getConfiguration().isOfflineAutoComplete()) {
      plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

        @Override
        public void run() {
          setupAutoComplete();
        }
      });
    }
  }

  public void setupConsole() throws SQLException {
    // Get the console
    String name = plugin.getConsoleConfig().getName();
    UUID uuid = plugin.getConsoleConfig().getUuid();

    console = queryForId(UUIDUtils.toBytes(uuid));

    if (console == null) {
      // Create it
      console = new PlayerData(uuid, name);
      create(console);
    } else if (!console.getName().equals(name)) {
      console.setName(name);
      plugin.getLogger().info("Console name change detected, updating database");
      update(console);
    }
  }

  public void setupAutoComplete() {
    autoCompleteTree = new ConcurrentRadixTree<>(new SmartArrayBasedNodeFactory());
    CloseableIterator<PlayerData> itr = null;

    try {
      itr = this.queryBuilder().selectColumns("name").iterator();

      while (itr.hasNext()) {
        autoCompleteTree.put(itr.next().getName(), VoidValue.SINGLETON);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }

  @Override
  public CreateOrUpdateStatus createOrUpdate(PlayerData data) throws SQLException {
    CreateOrUpdateStatus status = super.createOrUpdate(data);

    if (status.isCreated() && plugin.getConfiguration().isOfflineAutoComplete()) {
      autoCompleteTree.put(data.getName(), VoidValue.SINGLETON);
    }

    // Check for duplicates
    List<PlayerData> results = queryForEq("name", new SelectArg(data.getName()));
    if (results.size() == 1) return status;

    if (!plugin.getConfiguration().isOnlineMode()) {
      plugin.getLogger()
            .warning("Duplicates found for " + data.getName() + ", as you are in offline mode, please fix manually");
      return status;
    }

    // Duplicates found!
    for (PlayerData player : results) {
      if (player.getUUID().equals(data.getUUID())) continue;

      String newName;

      try {
        newName = UUIDUtils.getCurrentName(player.getUUID());
      } catch (Exception e) {
        plugin.getLogger()
              .warning("Duplicates found for " + data.getName() + ", was unable to contact Mojang for updated names");
        continue;
      }

      if (newName == null || newName.isEmpty() || player.getName().equals(newName)) continue;

      player.setName(newName);
      update(player);
    }

    return status;
  }

  public PlayerData createIfNotExists(UUID uuid, String name) throws SQLException {
    PlayerData player = queryForId(UUIDUtils.toBytes(uuid));

    if (player != null) return player;

    player = new PlayerData(uuid, name);
    create(player);

    if (plugin.getConfiguration().isOfflineAutoComplete()) autoCompleteTree.put(name, VoidValue.SINGLETON);

    return player;
  }

  public PlayerData retrieve(String name, boolean mojangLookup) {

    try {
      List<PlayerData> results = queryForEq("name", new SelectArg(name));
      if (results.size() == 1) {
        return results.get(0);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (!mojangLookup) {
      return null;
    }

    // UUID Lookup :(
    try {
      UUIDProfile player = UUIDUtils.getUUIDOf(name);
      if (player == null) {
        return null;
      }

      // Lets store for caching
      PlayerData data = new PlayerData(player.getUuid(), player.getName());

      super.createOrUpdate(data);

      return data;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public List<PlayerData> retrieve(String name) {
    try {
      return queryForEq("name", new SelectArg(name));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public List<PlayerData> getDuplicates(long ip) {
    ArrayList<PlayerData> players = new ArrayList<>();

    if (plugin.getConfiguration().getBypassPlayerIps().contains(ip)) {
      return players;
    }

    QueryBuilder<PlayerData, byte[]> query = queryBuilder();
    try {
      query.leftJoin(plugin.getPlayerBanStorage().queryBuilder());

      Where<PlayerData, byte[]> where = query.where();

      where.eq("ip", ip);

      query.setWhere(where);
    } catch (SQLException e) {
      e.printStackTrace();
      return players;
    }


    CloseableIterator<PlayerData> itr = null;
    try {
      itr = query.limit(300L).iterator();

      while (itr.hasNext()) {
        players.add(itr.next());
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

    return players;
  }
  
  public List<PlayerData> getDuplicatesInTime(long ip, long timediff) {
    ArrayList<PlayerData> players = new ArrayList<>();
    long currentTime = System.currentTimeMillis() / 1000L;

    if (plugin.getConfiguration().getBypassPlayerIps().contains(ip)) {
      return players;
    }

    QueryBuilder<PlayerData, byte[]> query = queryBuilder();
    try {
      query.leftJoin(plugin.getPlayerBanStorage().queryBuilder());

      Where<PlayerData, byte[]> where = query.where();

      where.eq("ip", ip).and().ge("lastSeen", (currentTime - timediff));

      query.setWhere(where);
    } catch (SQLException e) {
      e.printStackTrace();
      return players;
    }


    CloseableIterator<PlayerData> itr = null;
    try {
      itr = query.limit(300L).iterator();

      while (itr.hasNext()) {
        players.add(itr.next());
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

    return players;
  }

  public List<byte[]> getOnlineIds(Collection<? extends Player> onlinePlayers) {
    ArrayList<byte[]> ids = new ArrayList<>(onlinePlayers.size());

    for (Player player : onlinePlayers) {
      ids.add(UUIDUtils.toBytes(player));
    }

    return ids;
  }
}
