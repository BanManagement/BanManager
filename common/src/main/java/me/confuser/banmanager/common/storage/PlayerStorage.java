package me.confuser.banmanager.common.storage;

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
import inet.ipaddr.IPAddress;
import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.StorageUtils;
import me.confuser.banmanager.common.util.UUIDProfile;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.*;

public class PlayerStorage extends BaseDaoImpl<PlayerData, byte[]> {

  @Getter
  private BanManagerPlugin plugin;

  @Getter
  private RadixTree<VoidValue> autoCompleteTree;

  @Getter
  private PlayerData console;

  public PlayerStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerData>) plugin.getConfig().getLocalDb()
        .getTable("players"));

    this.plugin = plugin;

    if (!isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      StorageUtils.convertIpColumn(plugin, tableConfig.getTableName(), "ip", "bytes");
    }

    setupConsole();

    if (plugin.getConfig().isOfflineAutoComplete()) {
      // @TODO run in a separate thread to speed up start up
      setupAutoComplete();
    }
  }

  public PlayerStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerData>) table);
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

      // Confirm player with name does not already exist
      PlayerData data = retrieve(name, false);

      if (data != null && data.getName().equals(name)) {
        plugin.getLogger().severe("Unable to update Console name as a player already exists with this name");
      } else {
        update(console);
      }
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

  public CreateOrUpdateStatus upsert(PlayerData data) throws SQLException {
    if(data == null) return new CreateOrUpdateStatus(false, false, 0);

    PlayerData existingData = queryForSameId(data);
    if(existingData == null) {
      int rows = create(data);

      if(plugin.getConfig().isOfflineAutoComplete()) {
        autoCompleteTree.put(data.getName(), VoidValue.SINGLETON);
      }

      return new CreateOrUpdateStatus(true, false, rows);
    }

    int rows = update(data);

    if(plugin.getConfig().isOfflineAutoComplete()) {
      autoCompleteTree.remove(existingData.getName());
      autoCompleteTree.put(data.getName(), VoidValue.SINGLETON);
    }

    return new CreateOrUpdateStatus(false, true, rows);
  }

  @Override
  public CreateOrUpdateStatus createOrUpdate(PlayerData data) throws SQLException {
    CreateOrUpdateStatus status = upsert(data);

    // Check for duplicates
    List<PlayerData> results = queryForEq("name", new SelectArg(data.getName()));
    if (results.size() == 1) return status;

    if (!plugin.getConfig().isOnlineMode()) {
      plugin.getLogger()
          .warning("Duplicates found for " + data.getName() + ", as you are in offline mode, please fix manually https://banmanagement.com/faq#duplicate-issues");
      return status;
    }

    // Duplicates found!
    for (PlayerData player : results) {
      if (player.getUUID().equals(data.getUUID())) continue;

      String newName;

      try {
        newName = UUIDUtils.getCurrentName(plugin, player.getUUID());
      } catch (Exception e) {
        plugin.getLogger()
            .warning("Duplicates found for " + data.getName() + ", unable to contact Mojang for updated names https://banmanagement.com/faq#duplicate-issues");
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

    if (plugin.getConfig().isOfflineAutoComplete()) autoCompleteTree.put(name, VoidValue.SINGLETON);

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
      UUIDProfile player = UUIDUtils.getUUIDOf(plugin, name);
      if (player == null) {
        return null;
      }

      // Lets store for caching
      PlayerData data = new PlayerData(player.getUuid(), player.getName());

      upsert(data);

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

  public List<PlayerData> getDuplicates(IPAddress ip) {
    ArrayList<PlayerData> players = new ArrayList<>();

    if (plugin.getConfig().getBypassPlayerIps().contains(ip.toString())) {
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
        PlayerData player = itr.next();

        if (!plugin.getExemptionsConfig().isExempt(player, "alts")) players.add(player);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

    return players;
  }

  public HashMap<String, Map.Entry<Integer, List<PlayerData>>> getDuplicateNames() {
    HashMap<String, Map.Entry<Integer, List<PlayerData>>> duplicates = new HashMap<>();
    CloseableIterator<String[]> itr = null;

    try {
      itr = queryRaw("SELECT name, COUNT(name) FROM " + getTableName() + " GROUP BY name HAVING COUNT(name) > 1 ORDER BY name ASC LIMIT 10").closeableIterator();

      while (itr.hasNext()) {
        String[] values = itr.next();

        List<PlayerData> results = queryForEq("name", new SelectArg(values[0]));

        duplicates.put(values[0], new AbstractMap.SimpleEntry(Integer.parseInt(values[1]), results));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

    return duplicates;
  }

  public List<PlayerData> getDuplicatesInTime(IPAddress ip, long timeDiff) {
    ArrayList<PlayerData> players = new ArrayList<>();
    long currentTime = System.currentTimeMillis() / 1000L;

    if (plugin.getConfig().getBypassPlayerIps().contains(ip.toString())) {
      return players;
    }

    QueryBuilder<PlayerData, byte[]> query = queryBuilder();
    try {
      query.leftJoin(plugin.getPlayerBanStorage().queryBuilder());

      Where<PlayerData, byte[]> where = query.where();

      where.eq("ip", ip).and().ge("lastSeen", (currentTime - timeDiff));

      query.setWhere(where);
    } catch (SQLException e) {
      e.printStackTrace();
      return players;
    }


    CloseableIterator<PlayerData> itr = null;
    try {
      itr = query.limit(300L).iterator();

      while (itr.hasNext()) {
        PlayerData player = itr.next();

        if (!plugin.getExemptionsConfig().isExempt(player, "alts")) players.add(player);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

    return players;
  }
}
