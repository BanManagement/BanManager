package me.confuser.banmanager.storage.conversion.converters;

import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerKickData;

import java.sql.SQLException;

public class KickConverter extends Converter {

  @Override
  public void run(DatabaseConnection connection) {
    DatabaseResults result;
    try {
      result = connection.compileStatement("SELECT kicked, kicked_by, kick_reason, kick_time FROM " + conversionDb
              .getTableName("kicksTable"), StatementBuilder.StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS)
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
            plugin.getLogger().severe(name + " kick creation failed, unable to lookup UUID");
            continue;
          }
        }

        PlayerKickData kick = new PlayerKickData(player, actor, reason, created);

        // Disallow duplicates
        if (plugin.getPlayerKickStorage().queryForMatchingArgs(kick).size() != 0) continue;

        plugin.getPlayerKickStorage().create(kick);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }
  }
}
