package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ormlite.field.SqlType;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.support.CompiledStatement;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;
import me.confuser.banmanager.common.util.IPUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityStorage {

  // Queries
  private final String sinceSql;
  private final String sincePlayerSql;
  private BanManagerPlugin plugin;

  public ActivityStorage(BanManagerPlugin plugin) {
    this.plugin = plugin;

    sinceSql = "SELECT  type, name, actor, created, name2 FROM" +
            "  ( SELECT 'Ban' AS type, p.name AS name, actor.name AS actor, created, '' AS name2" +
            "    FROM " + plugin.getPlayerBanStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE created >= ?" +

            "    UNION ALL" +

            "    SELECT 'Ban' AS type, p.name AS name, actor.name AS actor, pastCreated AS created, '' AS name2" +
            "    FROM " + plugin.getPlayerBanRecordStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE pastCreated >= ?" +

            "    UNION ALL" +

            "    SELECT 'Unban' AS type, p.name AS name, actor.name AS actor, created, '' AS name2" +
            "    FROM " + plugin.getPlayerBanRecordStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE created >= ?" +

            "    UNION ALL" +

            "    SELECT 'Warning' AS type, p.name AS name, actor.name AS actor, created, '' AS name2" +
            "    FROM " + plugin.getPlayerWarnStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE created >= ?" +

            "    UNION ALL" +

            "    SELECT 'Mute' AS type, p.name AS name, actor.name AS actor, created, '' AS name2" +
            "    FROM " + plugin.getPlayerMuteStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE created >= ?" +

            "    UNION ALL" +

            "    SELECT 'Mute' AS type, p.name AS name, actor.name AS actor, pastCreated as created, '' AS name2" +
            "    FROM " + plugin.getPlayerMuteRecordStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE pastCreated >= ?" +

            "    UNION ALL" +

            "    SELECT 'Unmute' AS type, p.name AS name, actor.name AS actor, created, '' AS name2" +
            "    FROM " + plugin.getPlayerMuteRecordStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE created >= ?" +

            "    UNION ALL" +

            "    SELECT 'Note' AS type, p.name AS name, actor.name AS actor, created, '' AS name2" +
            "    FROM " + plugin.getPlayerNoteStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE created >= ?" +

            "    UNION ALL" +

            "    SELECT 'Ban' AS type, ib.ip AS name, actor.name AS actor, created, '' AS name2" +
            "    FROM " + plugin.getIpBanStorage().getTableConfig().getTableName() + " ib" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE created >= ?" +

            "    UNION ALL" +

            "    SELECT 'Ban' AS type, ibr.ip AS name, actor.name AS actor, pastCreated AS created, '' AS name2" +
            "    FROM " + plugin.getIpBanRecordStorage().getTableConfig().getTableName() + " ibr" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE pastCreated >= ?" +

            "    UNION ALL" +

            "    SELECT 'Unban' AS type, ibr.ip AS name, actor.name AS actor, created, '' AS name2" +
            "    FROM " + plugin.getIpBanRecordStorage().getTableConfig().getTableName() + " ibr" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE created >= ?" +

            "    UNION ALL" +

            "    SELECT 'Ban' AS type, fromIp AS name, actor.name AS actor, created, toIp AS name2" +
            "    FROM " + plugin.getIpRangeBanStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE created >= ?" +

            "    UNION ALL" +

            "    SELECT 'Ban' AS type, fromIp AS name, actor.name AS actor, pastCreated AS created, toIp AS name2" +
            "    FROM " + plugin.getIpRangeBanRecordStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE pastCreated >= ?" +

            "    UNION ALL" +

            "    SELECT 'Unban' AS type, fromIp AS name, actor.name AS actor, created, toIp AS name2" +
            "    FROM " + plugin.getIpRangeBanRecordStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE created >= ?" +

            "  ) subquery" +
            " ORDER BY created DESC";
    sincePlayerSql = "SELECT  type, name, created, name2 FROM" +
            "  ( SELECT 'Ban' AS type, p.name AS name, created, '' AS name2" +
            "    FROM " + plugin.getPlayerBanStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    WHERE created >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Ban' AS type, p.name AS name, pastCreated as created, '' AS name2" +
            "    FROM " + plugin.getPlayerBanRecordStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    WHERE pastCreated >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Unban' AS type, p.name AS name, created, '' AS name2" +
            "    FROM " + plugin.getPlayerBanRecordStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    WHERE created >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Warning' AS type, p.name AS name, created, '' AS name2" +
            "    FROM " + plugin.getPlayerWarnStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    WHERE created >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Mute' AS type, p.name AS name, created, '' AS name2" +
            "    FROM " + plugin.getPlayerMuteStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    WHERE created >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Mute' AS type, p.name AS name, pastCreated as created, '' AS name2" +
            "    FROM " + plugin.getPlayerMuteRecordStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    WHERE pastCreated >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Unmute' AS type, p.name AS name, created, '' AS name2" +
            "    FROM " + plugin.getPlayerMuteRecordStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    WHERE created >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Note' AS type, p.name AS name, created, '' AS name2" +
            "    FROM " + plugin.getPlayerNoteStorage().getTableConfig().getTableName() +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
            "    WHERE created >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Ban' AS type, ib.ip AS name, created, '' AS name2" +
            "    FROM " + plugin.getIpBanStorage().getTableConfig().getTableName() + " ib" +
            "    WHERE created >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Ban' AS type, ibr.ip AS name, pastCreated AS created, '' AS name2" +
            "    FROM " + plugin.getIpBanRecordStorage().getTableConfig().getTableName() + " ibr" +
            "    WHERE pastCreated >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Unban' AS type, ibr.ip AS name, created, '' AS name2" +
            "    FROM " + plugin.getIpBanRecordStorage().getTableConfig().getTableName() + " ibr" +
            "    WHERE created >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Ban' AS type, fromIp AS name, created, toIp AS name2" +
            "    FROM " + plugin.getIpRangeBanStorage().getTableConfig().getTableName() +
            "    WHERE created >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Ban' AS type, fromIp AS name, pastCreated AS created, toIp AS name2" +
            "    FROM " + plugin.getIpRangeBanRecordStorage().getTableConfig().getTableName() +
            "    WHERE pastCreated >= ? AND actor_id = ?" +

            "    UNION ALL" +

            "    SELECT 'Unban' AS type, fromIp AS name, created, toIp AS name2" +
            "    FROM " + plugin.getIpRangeBanRecordStorage().getTableConfig().getTableName() +
            "    WHERE created >= ? AND actor_id = ?" +

            "  ) subquery" +
            " ORDER BY created DESC";
  }

  public List<Map<String, Object>> getSince(long since) {
    return getSince(since, null);
  }

  public List<Map<String, Object>> getSince(long since, PlayerData actor) {
    DatabaseConnection connection;

    try {
      connection = plugin.getLocalConn().getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();

      return null;
    }

    final DatabaseResults result;
    boolean hasActor = actor != null;

    try {
      CompiledStatement statement = connection
              .compileStatement(hasActor ? sincePlayerSql : sinceSql, StatementBuilder.StatementType.SELECT, null,
                      DatabaseConnection.DEFAULT_RESULT_FLAGS, false);

      int maxItems = hasActor ? 28 : 14;

      for (int i = 0; i < maxItems; i++) {
        statement.setObject(i, since, SqlType.LONG);
        if (hasActor) {
          i++;
          statement.setObject(i, actor.getId(), SqlType.BYTE_ARRAY);
        }
      }
      result = statement.runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();

      try {
        plugin.getLocalConn().releaseConnection(connection);
      } catch (SQLException e1) {
        e1.printStackTrace();
      }

      return null;
    }

    List<Map<String, Object>> results = new ArrayList<>();

    try {
      while (result.next()) {
        Map<String, Object> map = new HashMap<>(hasActor ? 3 : 4);

        int ipIndex = 3;
        map.put("type", result.getString(0));

        if (hasActor) {
          map.put("created", result.getLong(2));
        } else {
          map.put("actor", result.getString(2));
          map.put("created", result.getLong(3));
          ipIndex = 4;
        }

        // Detect names vs ips
        try {
          String ip = IPUtils.toString(result.getBytes(1));

          if (!result.getString(ipIndex).isEmpty()) {
            ip = ip + " - " + IPUtils.toString(result.getBytes(ipIndex));
          }

          map.put("player", ip);
        } catch (Exception e) {
          map.put("player", result.getString(1));
        }
        results.add(map);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }

    try {
      plugin.getLocalConn().releaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return results;
  }
}
