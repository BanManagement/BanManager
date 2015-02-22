package me.confuser.banmanager.storage.conversion.converters;

import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;

import java.sql.SQLException;

public class MuteConverter extends Converter {

  @Override
  public void run(DatabaseConnection connection) {
    DatabaseResults result;
    try {
      result = connection
              .compileStatement("SELECT muted, muted_by, mute_reason, mute_time, mute_expires_on FROM " + conversionDb
                      .getTableName("mutesTable"), StatementBuilder.StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS)
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
        long expires = result.getLong(4);

        PlayerData player = playerStorage.retrieve(name, false);
        PlayerData actor = playerStorage.retrieve(actorName, false);

        if (actor == null) {
          actor = playerStorage.getConsole();
        }

        if (player == null) {
          player = findAndCreate(name, created);

          if (player == null) {
            plugin.getLogger().severe(name + " mute creation failed, unable to lookup UUID");
            continue;
          }
        }

        if (plugin.getPlayerMuteStorage().isMuted(player.getUUID())) continue;

        PlayerMuteData mute = new PlayerMuteData(player, actor, reason, expires, created);

        plugin.getPlayerMuteStorage().create(mute);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }
  }
}
