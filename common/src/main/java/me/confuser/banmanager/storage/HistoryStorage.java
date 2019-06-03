package me.confuser.banmanager.storage;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.parsers.InfoCommandParser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class HistoryStorage {

  private BanManagerPlugin plugin = BanManager.getPlugin();
  private ConnectionSource localConn;

  // Queries
  final String banSql = "SELECT t.id, 'Ban' type, actor.name AS actor, pastCreated as created, reason, '' AS meta" +
          "    FROM " + plugin.getPlayerBanRecordStorage().getTableConfig().getTableName() + " t" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON pastActor_id = actor.id" +
          "    WHERE player_id = ?";
  final String muteSql = "SELECT t.id, 'Mute' type, actor.name AS actor, pastCreated as created, reason, '' AS meta" +
          "    FROM " + plugin.getPlayerMuteRecordStorage().getTableConfig().getTableName() + " t" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON pastActor_id = actor.id" +
          "    WHERE player_id = ?";
  final String kickSql = "SELECT t.id, 'Kick' type, actor.name AS actor, created, reason, '' AS meta" +
          "    FROM " + plugin.getPlayerKickStorage().getTableConfig().getTableName() + " t" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE player_id = ?";
  final String warningSql = "SELECT t.id, 'Warning' type, actor.name AS actor, created, reason, points AS meta" +
          "    FROM " + plugin.getPlayerWarnStorage().getTableConfig().getTableName() + " t" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE player_id = ?";
  final String noteSql = "SELECT t.id, 'Note' type, actor.name AS actor, created, message AS reason, '' AS meta" +
          "    FROM " + plugin.getPlayerNoteStorage().getTableConfig().getTableName() + " t" +
          "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                   .getTableName() + " actor ON actor_id = actor.id" +
          "    WHERE player_id = ?";

  private final String playerSql = "SELECT id, type, actor, created, reason, meta FROM" +
          "  ( {QUERIES}" +
          "  ) subquery" +
          " ORDER BY created DESC, FIELD(type, 'Ban', 'Warning', 'Mute', 'Kick', 'Note')";

  public HistoryStorage(ConnectionSource localConn) {
    this.localConn = localConn;
  }

  public ArrayList<HashMap<String, Object>> getSince(PlayerData player, long since, InfoCommandParser parser) {
    DatabaseConnection connection;

    try {
      connection = localConn.getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();

      return null;
    }

    final DatabaseResults result;
    String sql;
    StringBuilder unions = new StringBuilder();
    int typeCount = 0;

    // TODO refactor
    if (parser.isBans()) {
      unions.append(banSql);
      unions.append(" AND created >= ").append(since);
      unions.append(" UNION ALL ");
      typeCount++;
    }

    if (parser.isMutes()) {
      unions.append(muteSql);
      unions.append(" AND created >= ").append(since);
      unions.append(" UNION ALL ");
      typeCount++;
    }
    if (parser.isKicks()) {
      unions.append(kickSql);
      unions.append(" AND created >= ").append(since);
      unions.append(" UNION ALL ");
      typeCount++;
    }
    if (parser.isNotes()) {
      unions.append(noteSql);
      unions.append(" AND created >= ").append(since);
      unions.append(" UNION ALL ");
      typeCount++;
    }
    if (parser.isWarnings()) {
      unions.append(warningSql);
      unions.append(" AND created >= ").append(since);
      unions.append(" UNION ALL ");
      typeCount++;
    }

    unions.setLength(unions.length() - 11);

    sql = playerSql.replace("{QUERIES}", unions.toString());

    try {
      CompiledStatement statement = connection
              .compileStatement(sql, StatementBuilder.StatementType.SELECT, null, DatabaseConnection
                      .DEFAULT_RESULT_FLAGS, false);

      for (int i = 0; i < typeCount; i++) {
        statement.setObject(i, player.getId(), SqlType.BYTE_ARRAY);
      }

      result = statement.runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();

      try {
        localConn.releaseConnection(connection);
      } catch (SQLException e1) {
        e1.printStackTrace();
      }

      return null;
    }

    ArrayList<HashMap<String, Object>> results = new ArrayList<>();

    try {
      while (result.next()) {
        results.add(new HashMap<String, Object>(4) {

          {
            put("id", result.getInt(0));
            put("type", result.getString(1));
            put("actor", result.getString(2));
            put("created", result.getLong(3));

            if (result.getString(1).equals("Note")) {
              put("reason", ChatColor.translateAlternateColorCodes('&', result.getString(4)));
            } else {
              put("reason", result.getString(4));
            }

            put("meta", result.getString(5));
          }
        });
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }

    try {
      localConn.releaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return results;
  }

  public ArrayList<HashMap<String, Object>> getAll(PlayerData player, InfoCommandParser parser) {
    DatabaseConnection connection;

    try {
      connection = localConn.getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();

      return null;
    }

    final DatabaseResults result;
    String sql;
    StringBuilder unions = new StringBuilder();
    int typeCount = 0;

    // TODO refactor
    if (parser.isBans()) {
      unions.append(banSql);
      unions.append(" UNION ALL ");
      typeCount++;
    }

    if (parser.isMutes()) {
      unions.append(muteSql);
      unions.append(" UNION ALL ");
      typeCount++;
    }
    if (parser.isKicks()) {
      unions.append(kickSql);
      unions.append(" UNION ALL ");
      typeCount++;
    }
    if (parser.isNotes()) {
      unions.append(noteSql);
      unions.append(" UNION ALL ");
      typeCount++;
    }
    if (parser.isWarnings()) {
      unions.append(warningSql);
      unions.append(" UNION ALL ");
      typeCount++;
    }

    unions.setLength(unions.length() - 11);

    sql = playerSql.replace("{QUERIES}", unions.toString());

    try {
      CompiledStatement statement = connection
              .compileStatement(sql, StatementBuilder.StatementType.SELECT, null, DatabaseConnection
                      .DEFAULT_RESULT_FLAGS, false);

      for (int i = 0; i < typeCount; i++) {
        statement.setObject(i, player.getId(), SqlType.BYTE_ARRAY);
      }

      result = statement.runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();

      try {
        localConn.releaseConnection(connection);
      } catch (SQLException e1) {
        e1.printStackTrace();
      }

      return null;
    }

    ArrayList<HashMap<String, Object>> results = new ArrayList<>();

    try {
      while (result.next()) {
        results.add(new HashMap<String, Object>(4) {

          {
            put("id", result.getInt(0));
            put("type", result.getString(1));
            put("actor", result.getString(2));
            put("created", result.getLong(3));
            put("reason", result.getString(4));
            put("meta", result.getString(5));
          }
        });
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }

    try {
      localConn.releaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return results;
  }
}
