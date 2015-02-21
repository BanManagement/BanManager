package me.confuser.banmanager.storage.conversion.converters;

import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerWarnData;

import java.sql.SQLException;

public class WarningConverter extends Converter {

  @Override
  public void run(DatabaseConnection connection) {
    DatabaseResults result;
    try {
      result = connection.compileStatement("SELECT warned, warned_by, warn_reason, warn_time FROM " + conversionDb
              .getTableName("warningsTable"), StatementBuilder.StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS)
                         .runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    try {
      while (result.next()) {
        String name = result.getString(0);
        String actorName = result.getString(1);
        String reason = result.getString(2);
        long created = result.getLong(3);

        PlayerData player = playerStorage.retrieve(name, false);
        PlayerData actor = playerStorage.retrieve(actorName, false);

        if (actor == null) {
          actor = playerStorage.getConsole();
        }

        if (player == null) {
          player = findAndCreate(name, created);

          if (player == null) {
            plugin.getLogger().severe(name + " warning creation failed, unable to lookup UUID");
            continue;
          }
        }

        PlayerWarnData warn = new PlayerWarnData(player, actor, reason, true, created);

        // Disallow duplicates
        if (plugin.getPlayerWarnStorage().queryForMatchingArgs(warn).size() != 0) continue;

        plugin.getPlayerWarnStorage().create(warn);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }
  }
}
