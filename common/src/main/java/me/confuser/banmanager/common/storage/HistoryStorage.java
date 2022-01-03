package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ormlite.field.SqlType;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.support.CompiledStatement;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;
import me.confuser.banmanager.common.util.parsers.InfoCommandParser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class HistoryStorage {

  private final String playerSql = "SELECT id, type, actor, created, reason, meta FROM" +
          "  ( {QUERIES}" +
          "  ) subquery" +
          " ORDER BY created DESC";
  private BanManagerPlugin plugin;
  // Queries
  private final String banSql;
  private final String muteSql;
  private final String kickSql;
  private final String warningSql;
  private final String noteSql;

  private final String ipBanSql;
  private final String ipMuteSql;
  private final String ipRangeBanSql;


  public HistoryStorage(BanManagerPlugin plugin) {
    this.plugin = plugin;

    banSql = "SELECT t.id, 'Ban' AS type, actor.name AS actor, pastCreated as created, reason, '' AS meta" +
            "    FROM " + plugin.getPlayerBanRecordStorage().getTableConfig().getTableName() + " t" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON pastActor_id = actor.id" +
            "    WHERE player_id = ?";
    muteSql = "SELECT t.id, 'Mute' AS type, actor.name AS actor, pastCreated as created, reason, '' AS meta" +
            "    FROM " + plugin.getPlayerMuteRecordStorage().getTableConfig().getTableName() + " t" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON pastActor_id = actor.id" +
            "    WHERE player_id = ?";
    kickSql = "SELECT t.id, 'Kick' AS type, actor.name AS actor, created, reason, '' AS meta" +
            "    FROM " + plugin.getPlayerKickStorage().getTableConfig().getTableName() + " t" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE player_id = ?";
    warningSql = "SELECT t.id, 'Warning' AS type, actor.name AS actor, created, reason, points AS meta" +
            "    FROM " + plugin.getPlayerWarnStorage().getTableConfig().getTableName() + " t" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE player_id = ?";
    noteSql = "SELECT t.id, 'Note' AS type, actor.name AS actor, created, message AS reason, '' AS meta" +
            "    FROM " + plugin.getPlayerNoteStorage().getTableConfig().getTableName() + " t" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    WHERE player_id = ?";

    ipBanSql = "SELECT t.id, 'IP Ban' AS type, actor.name AS actor, pastCreated as created, reason, '' AS meta" +
        "    FROM " + plugin.getIpBanRecordStorage().getTableConfig().getTableName() + " t" +
        "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
        .getTableName() + " actor ON pastActor_id = actor.id" +
        "    WHERE t.ip = ?";

    ipMuteSql = "SELECT t.id, 'IP Mute' AS type, actor.name AS actor, pastCreated as created, reason, '' AS meta" +
        "    FROM " + plugin.getIpMuteRecordStorage().getTableConfig().getTableName() + " t" +
        "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
        .getTableName() + " actor ON pastActor_id = actor.id" +
        "    WHERE t.ip = ?";

    ipRangeBanSql = "SELECT t.id, 'IP Range Ban' AS type, actor.name AS actor, pastCreated as created, reason, CONCAT(INET6_NTOA(fromIp), ' - ', INET6_NTOA(toIp)) AS meta" +
        "    FROM " + plugin.getIpRangeBanRecordStorage().getTableConfig().getTableName() + " t" +
        "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
        .getTableName() + " actor ON pastActor_id = actor.id" +
        "    WHERE ? BETWEEN fromIp AND toIp";
  }

  public ArrayList<HashMap<String, Object>> getSince(PlayerData player, long since, InfoCommandParser parser) {
    DatabaseConnection connection;

    try {
      connection = plugin.getLocalConn().getReadOnlyConnection("");
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
        plugin.getLocalConn().releaseConnection(connection);
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
      plugin.getLocalConn().releaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return results;
  }

  public ArrayList<HashMap<String, Object>> getAll(PlayerData player, InfoCommandParser parser) {
    DatabaseConnection connection;

    try {
      connection = plugin.getLocalConn().getReadOnlyConnection("");
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
        plugin.getLocalConn().releaseConnection(connection);
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
      plugin.getLocalConn().releaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return results;
  }

  public ArrayList<HashMap<String, Object>> getSince(IPAddress ip, long since, InfoCommandParser parser) {
    DatabaseConnection connection;

    try {
      connection = plugin.getLocalConn().getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();

      return null;
    }

    final DatabaseResults result;
    String sql;
    StringBuilder unions = new StringBuilder();
    int typeCount = 0;

    if (parser.isBans()) {
      unions.append(ipBanSql);
      unions.append(" AND created >= ").append(since);
      unions.append(" UNION ALL ");
      typeCount++;

      unions.append(ipRangeBanSql);
      unions.append(" AND created >= ").append(since);
      unions.append(" UNION ALL ");
      typeCount++;
    }

    if (parser.isMutes()) {
      unions.append(ipMuteSql);
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
        statement.setObject(i, ip.getBytes(), SqlType.BYTE_ARRAY);
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
      plugin.getLocalConn().releaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return results;
  }

  public ArrayList<HashMap<String, Object>> getAll(IPAddress ip, InfoCommandParser parser) {
    DatabaseConnection connection;

    try {
      connection = plugin.getLocalConn().getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();

      return null;
    }

    final DatabaseResults result;
    String sql;
    StringBuilder unions = new StringBuilder();
    int typeCount = 0;

    if (parser.isBans()) {
      unions.append(ipBanSql);
      unions.append(" UNION ALL ");
      typeCount++;

      unions.append(ipRangeBanSql);
      unions.append(" UNION ALL ");
      typeCount++;
    }

    if (parser.isMutes()) {
      unions.append(ipMuteSql);
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
        statement.setObject(i, ip.getBytes(), SqlType.BYTE_ARRAY);
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
      plugin.getLocalConn().releaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return results;
  }
}
