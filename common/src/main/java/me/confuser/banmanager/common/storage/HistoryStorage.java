package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.HistoryEntry;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ormlite.field.SqlType;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.support.CompiledStatement;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;
import me.confuser.banmanager.common.util.parsers.InfoCommandParser;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HistoryStorage {

  private final String playerSql = "SELECT id, type, actor, created, reason, meta FROM" +
          "  ( {QUERIES}" +
          "  ) subquery" +
          " ORDER BY created DESC";
  private BanManagerPlugin plugin;

  private final String banSql;
  private final String muteSql;
  private final String kickSql;
  private final String warningSql;
  private final String noteSql;
  private final String reportSql;

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
    muteSql = "SELECT t.id, 'Mute' AS type, actor.name AS actor, pastCreated as created, reason, CASE WHEN t.onlineOnly = 1 THEN '(online-only)' ELSE '' END AS meta" +
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
    reportSql = "SELECT t.id, 'Reported' AS type, actor.name AS actor, created, reason, state.name AS meta" +
            "    FROM " + plugin.getPlayerReportStorage().getTableConfig().getTableName() + " t" +
            "    LEFT JOIN " + plugin.getPlayerStorage().getTableConfig()
                                     .getTableName() + " actor ON actor_id = actor.id" +
            "    LEFT JOIN " + plugin.getReportStateStorage().getTableConfig()
                                      .getTableName() + " state ON t.state_id = state.id" +
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

  public List<HistoryEntry> getSince(PlayerData player, long since, InfoCommandParser parser) {
    StringBuilder unions = new StringBuilder();
    int typeCount = 0;

    if (parser.isBans()) { unions.append(banSql).append(" AND created >= ").append(since).append(" UNION ALL "); typeCount++; }
    if (parser.isMutes()) { unions.append(muteSql).append(" AND created >= ").append(since).append(" UNION ALL "); typeCount++; }
    if (parser.isKicks()) { unions.append(kickSql).append(" AND created >= ").append(since).append(" UNION ALL "); typeCount++; }
    if (parser.isNotes()) { unions.append(noteSql).append(" AND created >= ").append(since).append(" UNION ALL "); typeCount++; }
    if (parser.isReports()) { unions.append(reportSql).append(" AND created >= ").append(since).append(" UNION ALL "); typeCount++; }
    if (parser.isWarnings()) { unions.append(warningSql).append(" AND created >= ").append(since).append(" UNION ALL "); typeCount++; }

    return executeQuery(player.getId(), SqlType.BYTE_ARRAY, unions, typeCount);
  }

  public List<HistoryEntry> getAll(PlayerData player, InfoCommandParser parser) {
    StringBuilder unions = new StringBuilder();
    int typeCount = 0;

    if (parser.isBans()) { unions.append(banSql).append(" UNION ALL "); typeCount++; }
    if (parser.isMutes()) { unions.append(muteSql).append(" UNION ALL "); typeCount++; }
    if (parser.isKicks()) { unions.append(kickSql).append(" UNION ALL "); typeCount++; }
    if (parser.isNotes()) { unions.append(noteSql).append(" UNION ALL "); typeCount++; }
    if (parser.isReports()) { unions.append(reportSql).append(" UNION ALL "); typeCount++; }
    if (parser.isWarnings()) { unions.append(warningSql).append(" UNION ALL "); typeCount++; }

    return executeQuery(player.getId(), SqlType.BYTE_ARRAY, unions, typeCount);
  }

  public List<HistoryEntry> getSince(IPAddress ip, long since, InfoCommandParser parser) {
    StringBuilder unions = new StringBuilder();
    int typeCount = 0;

    if (parser.isBans()) {
      unions.append(ipBanSql).append(" AND created >= ").append(since).append(" UNION ALL "); typeCount++;
      unions.append(ipRangeBanSql).append(" AND created >= ").append(since).append(" UNION ALL "); typeCount++;
    }
    if (parser.isMutes()) { unions.append(ipMuteSql).append(" AND created >= ").append(since).append(" UNION ALL "); typeCount++; }

    return executeQuery(ip.getBytes(), SqlType.BYTE_ARRAY, unions, typeCount);
  }

  public List<HistoryEntry> getAll(IPAddress ip, InfoCommandParser parser) {
    StringBuilder unions = new StringBuilder();
    int typeCount = 0;

    if (parser.isBans()) {
      unions.append(ipBanSql).append(" UNION ALL "); typeCount++;
      unions.append(ipRangeBanSql).append(" UNION ALL "); typeCount++;
    }
    if (parser.isMutes()) { unions.append(ipMuteSql).append(" UNION ALL "); typeCount++; }

    return executeQuery(ip.getBytes(), SqlType.BYTE_ARRAY, unions, typeCount);
  }

  private List<HistoryEntry> executeQuery(Object paramValue, SqlType paramType,
      StringBuilder unions, int typeCount) {
    if (typeCount == 0) {
      return new ArrayList<>();
    }

    unions.setLength(unions.length() - 11);
    String sql = playerSql.replace("{QUERIES}", unions.toString());

    try (DatabaseConnection connection = plugin.getLocalConn().getReadOnlyConnection("")) {
      CompiledStatement statement = connection.compileStatement(
          sql, StatementBuilder.StatementType.SELECT, null,
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false);

      List<HistoryEntry> results = new ArrayList<>();
      try {
        for (int i = 0; i < typeCount; i++) {
          statement.setObject(i, paramValue, paramType);
        }

        DatabaseResults dbResults = statement.runQuery(null);
        try {
          while (dbResults.next()) {
            results.add(new HistoryEntry(
                dbResults.getInt(0),
                dbResults.getString(1),
                dbResults.getString(2),
                dbResults.getLong(3),
                dbResults.getString(4),
                dbResults.getString(5)));
          }
        } finally {
          dbResults.closeQuietly();
        }
      } finally {
        try { statement.close(); } catch (IOException ignored) { }
      }

      return results;
    } catch (SQLException | IOException e) {
      plugin.getLogger().warning("Failed to process history operation", e);
      return null;
    }
  }
}
