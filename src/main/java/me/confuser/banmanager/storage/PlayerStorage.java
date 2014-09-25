package me.confuser.banmanager.storage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.UUIDProfile;
import me.confuser.banmanager.util.UUIDUtils;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

public class PlayerStorage extends BaseDaoImpl<PlayerData, byte[]> {

      private BanManager plugin = BanManager.getPlugin();
      private ConcurrentHashMap<UUID, PlayerData> online = new ConcurrentHashMap<>();
      private PlayerData console;

      public PlayerStorage(ConnectionSource connection, DatabaseTableConfig<PlayerData> tableConfig) throws SQLException {
            super(connection, tableConfig);
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

      public void addOnline(PlayerData player) {
            online.put(player.getUUID(), player);
      }

      public void addOnline(PlayerData player, boolean save) throws SQLException {
            createOrUpdate(player);

            addOnline(player);
      }

      public PlayerData removeOnline(UUID uuid) {
            return online.remove(uuid);
      }

      public boolean isOnline(UUID uuid) {
            return online.get(uuid) != null;
      }

      public boolean isOnline(Player player) {
            return isOnline(player.getUniqueId());
      }

      public PlayerData getOnline(UUID uuid) {
            return online.get(uuid);
      }

      public PlayerData getOnline(Player player) {
            return getOnline(player.getUniqueId());
      }

      public PlayerData getConsole() {
            return console;
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
                  // TODO Auto-generated catch block
                  e.printStackTrace();
            }

            return players;
      }
}
