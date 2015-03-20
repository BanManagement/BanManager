package me.confuser.banmanager.storage;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.SmartArrayBasedNodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.voidvalue.VoidValue;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStorage extends BaseDaoImpl<PlayerData, byte[]> {

  private BanManager plugin = BanManager.getPlugin();
  private ConcurrentHashMap<UUID, PlayerData> online = new ConcurrentHashMap<>();
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
    setupAutoComplete();
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
    }
  }

  public void setupAutoComplete() throws SQLException {
    autoCompleteTree = new ConcurrentRadixTree<>(new SmartArrayBasedNodeFactory());
    CloseableIterator<PlayerData> itr = this.queryBuilder().selectColumns("name").iterator();

    while (itr.hasNext()) {
      autoCompleteTree.put(itr.next().getName(), VoidValue.SINGLETON);
    }

    itr.close();
  }

  public void addOnline(PlayerData player) {
    online.put(player.getUUID(), player);
  }

  public PlayerData removeOnline(UUID uuid) {
    return online.remove(uuid);
  }

  public boolean isOnline(UUID uuid) {
    return online.get(uuid) != null;
  }

  public PlayerData getOnline(UUID uuid) {
    return online.get(uuid);
  }

  public PlayerData getOnline(Player player) {
    return getOnline(player.getUniqueId());
  }

  @Override
  public CreateOrUpdateStatus createOrUpdate(PlayerData data) throws SQLException {
    CreateOrUpdateStatus status = super.createOrUpdate(data);

    // Check for duplicates
    List<PlayerData> results = queryForEq("name", data.getName());
    if (results.size() == 1) return status;

    // Duplicates found!
    for (PlayerData player : results) {
      if (player.getUUID().equals(data.getUUID())) continue;

      String newName;

      try {
        newName = UUIDUtils.getCurrentName(player.getUUID());
      } catch (Exception e) {
        e.printStackTrace();
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

    return player;
  }

  public PlayerData retrieve(String name, boolean mojangLookup) {
    // Check if online first
    for (PlayerData player : online.values()) {
      if (player.getName().equalsIgnoreCase(name)) {
        return player;
      }
    }

    try {
      List<PlayerData> results = queryForEq("name", name);
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

      create(data);

      return data;
    } catch (Exception e) {
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

      CloseableIterator<PlayerData> itr = query.limit(300L).iterator();

      while (itr.hasNext()) {
        players.add(itr.next());
      }

      itr.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return players;
  }
}
