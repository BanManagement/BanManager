package me.confuser.banmanager.storage;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ActivityStorage {

  private BanManager plugin = BanManager.getPlugin();
  private JdbcPooledConnectionSource localConn;

  // Queries
  final String sinceSql = "SELECT  type, name, actor, created FROM" +
          "  ( SELECT 'Ban' type, p.name AS name, actor.name AS actor, created" +
          "    FROM " + plugin.getPlayerBanStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE created >= ?" +

          "    UNION ALL" +

          "    SELECT 'Ban' type, p.name AS name, actor.name AS actor, pastCreated as created" +
          "    FROM " + plugin.getPlayerBanRecordStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE pastCreated >= ?" +

          "    UNION ALL" +

          "    SELECT 'Unban' type, p.name AS name, actor.name AS actor, created" +
          "    FROM " + plugin.getPlayerBanRecordStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE created >= ?" +

          "    UNION ALL" +

          "    SELECT 'Warning' type, p.name AS name, actor.name AS actor, created" +
          "    FROM " + plugin.getPlayerWarnStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE created >= ?" +

          "    UNION ALL" +

          "    SELECT 'Mute' type, p.name AS name, actor.name AS actor, created" +
          "    FROM " + plugin.getPlayerMuteStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE created >= ?" +

          "    UNION ALL" +

          "    SELECT 'Mute' type, p.name AS name, actor.name AS actor, pastCreated as created" +
          "    FROM " + plugin.getPlayerMuteRecordStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE pastCreated >= ?" +

          "    UNION ALL" +

          "    SELECT 'Unmute' type, p.name AS name, actor.name AS actor, created" +
          "    FROM " + plugin.getPlayerMuteRecordStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE created >= ?" +

          "    UNION ALL" +

          "    SELECT 'Note' type, p.name AS name, actor.name AS actor, created" +
          "    FROM " + plugin.getPlayerNoteStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE created >= ?" +

          "    UNION ALL" +

          "    SELECT 'Ban' type, INET_NTOA(ib.ip) AS name, actor.name AS actor, created" +
          "    FROM " + plugin.getIpBanStorage().getTableConfig().getTableName() + " ib" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE created >= ?" +

          "    UNION ALL" +

          "    SELECT 'Ban' type, INET_NTOA(ibr.ip) AS name, actor.name AS actor, pastCreated AS created" +
          "    FROM " + plugin.getIpBanRecordStorage().getTableConfig().getTableName() + " ibr" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE pastCreated >= ?" +

          "    UNION ALL" +

          "    SELECT 'Unban' type, INET_NTOA(ibr.ip) AS name, actor.name AS actor, created" +
          "    FROM " + plugin.getIpBanRecordStorage().getTableConfig().getTableName() + " ibr" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE created >= ?" +

          "    UNION ALL" +

          "    SELECT 'Ban' type, CONCAT_WS(' - ', INET_NTOA(fromIp), INET_NTOA(toIp)) AS name, actor.name AS actor, created" +
          "    FROM " + plugin.getIpRangeBanStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE created >= ?" +

          "    UNION ALL" +

          "    SELECT 'Ban' type, CONCAT_WS(' - ', INET_NTOA(fromIp), INET_NTOA(toIp)) AS name, actor.name AS actor, pastCreated AS created" +
          "    FROM " + plugin.getIpRangeBanRecordStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE pastCreated >= ?" +

          "    UNION ALL" +

          "    SELECT 'Unban' type, CONCAT_WS(' - ', INET_NTOA(fromIp), INET_NTOA(toIp)) AS name, actor.name AS actor, created" +
          "    FROM " + plugin.getIpRangeBanRecordStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE created >= ?" +

          "  ) subquery" +
          " ORDER BY created DESC, FIELD(type, 'Ban', 'Unban', 'Warning', 'Mute', 'Unmute', 'Note')";

  private final String sincePlayerSql = "SELECT  type, name, created FROM" +
          "  ( SELECT 'Ban' type, p.name AS name, created" +
          "    FROM " + plugin.getPlayerBanStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    WHERE created >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Ban' type, p.name AS name, pastCreated as created" +
          "    FROM " + plugin.getPlayerBanRecordStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    WHERE pastCreated >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Unban' type, p.name AS name, created" +
          "    FROM " + plugin.getPlayerBanRecordStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    WHERE created >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Warning' type, p.name AS name, created" +
          "    FROM " + plugin.getPlayerWarnStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    WHERE created >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Mute' type, p.name AS name, created" +
          "    FROM " + plugin.getPlayerMuteStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    WHERE created >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Mute' type, p.name AS name, pastCreated as created" +
          "    FROM " + plugin.getPlayerMuteRecordStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    WHERE pastCreated >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Unmute' type, p.name AS name, created" +
          "    FROM " + plugin.getPlayerMuteRecordStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    WHERE created >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Note' type, p.name AS name, created" +
          "    FROM " + plugin.getPlayerNoteStorage().getTableConfig().getTableName() +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig().getTableName() + " p ON player_id = p.id" +
          "    WHERE created >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Ban' type, INET_NTOA(ib.ip) AS name, created" +
          "    FROM " + plugin.getIpBanStorage().getTableConfig().getTableName() + " ib" +
          "    WHERE created >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Ban' type, INET_NTOA(ibr.ip) AS name, pastCreated AS created" +
          "    FROM " + plugin.getIpBanRecordStorage().getTableConfig().getTableName() + " ibr" +
          "    WHERE pastCreated >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Unban' type, INET_NTOA(ibr.ip) AS name, created" +
          "    FROM " + plugin.getIpBanRecordStorage().getTableConfig().getTableName() + " ibr" +
          "    WHERE created >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Ban' type, CONCAT_WS(' - ', INET_NTOA(fromIp), INET_NTOA(toIp)) AS name, created" +
          "    FROM " + plugin.getIpRangeBanStorage().getTableConfig().getTableName() +
          "    WHERE created >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Ban' type, CONCAT_WS(' - ', INET_NTOA(fromIp), INET_NTOA(toIp)) AS name, pastCreated AS created" +
          "    FROM " + plugin.getIpRangeBanRecordStorage().getTableConfig().getTableName() +
          "    WHERE pastCreated >= ? AND actor_id = ?" +

          "    UNION ALL" +

          "    SELECT 'Unban' type, CONCAT_WS(' - ', INET_NTOA(fromIp), INET_NTOA(toIp)) AS name, created" +
          "    FROM " + plugin.getIpRangeBanRecordStorage().getTableConfig().getTableName() +
          "    WHERE created >= ? AND actor_id = ?" +

          "  ) subquery" +
          " ORDER BY created DESC, FIELD(type, 'Ban', 'Unban', 'Warning', 'Mute', 'Unmute', 'Note')";

  public ActivityStorage(JdbcPooledConnectionSource localConn) {
    this.localConn = localConn;
  }

  public ArrayList<HashMap<String, Object>> getSince(long since) {
    DatabaseConnection connection;

    try {
      connection = localConn.getReadOnlyConnection();
    } catch (SQLException e) {
      e.printStackTrace();

      return null;
    }

    final DatabaseResults result;

    try {
      CompiledStatement statement = connection
              .compileStatement(sinceSql, StatementBuilder.StatementType.SELECT, null, DatabaseConnection
                      .DEFAULT_RESULT_FLAGS);

      for (int i = 0; i < 14; i++) {
        statement.setObject(i, since, SqlType.LONG);
      }

      result = statement.runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();

      return null;
    }

    ArrayList<HashMap<String, Object>> results = new ArrayList<>();

    try {
      while (result.next()) {
        results.add(new HashMap<String, Object>(4) {

          {
            put("type", result.getString(0));
            put("player", result.getString(1));
            put("actor", result.getString(2));
            put("created", result.getLong(3));
          }
        });
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }

    connection.closeQuietly();

    return results;
  }

  public ArrayList<HashMap<String, Object>> getSince(long since, PlayerData actor) {
    DatabaseConnection connection;

    try {
      connection = localConn.getReadOnlyConnection();
    } catch (SQLException e) {
      e.printStackTrace();

      return null;
    }

    final DatabaseResults result;

    try {
      CompiledStatement statement = connection
              .compileStatement(sincePlayerSql, StatementBuilder.StatementType.SELECT, null, DatabaseConnection
                      .DEFAULT_RESULT_FLAGS);

      for (int i = 0; i < 28; i++) {
        statement.setObject(i, since, SqlType.LONG);
        i++;
        statement.setObject(i, actor.getId(), SqlType.BYTE_ARRAY);
      }

      result = statement.runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();

      return null;
    }

    ArrayList<HashMap<String, Object>> results = new ArrayList<>();

    try {
      while (result.next()) {
        results.add(new HashMap<String, Object>(4) {

          {
            put("type", result.getString(0));
            put("player", result.getString(1));
            put("created", result.getLong(2));
          }
        });
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }

    connection.closeQuietly();

    return results;
  }
}
