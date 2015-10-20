package me.confuser.banmanager.storage.conversion.converters;

import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.data.PlayerMuteRecord;

import java.sql.SQLException;

public class MuteRecordConverter extends Converter {

  @Override
  public void run(DatabaseConnection connection) {
    DatabaseResults result;
    try {
      result = connection
              .compileStatement("SELECT muted, muted_by, mute_reason, mute_time, mute_expired_on, unmuted_by, unmuted_time FROM " + conversionDb
                      .getTableName("mutesRecordTable"), StatementBuilder.StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS)
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
        long pastCreated = result.getLong(3);
        long expires = result.getLong(4);
        String unbannedActorName = result.getString(5);
        long created = result.getLong(6);

        PlayerData player = playerStorage.retrieve(name, false);
        PlayerData banActor = playerStorage.retrieve(actorName, false);
        PlayerData actor = playerStorage.retrieve(unbannedActorName, false);

        if (actor == null) {
          actor = playerStorage.getConsole();
        }

        if (banActor == null) {
          banActor = playerStorage.getConsole();
        }

        if (player == null) {
          player = findAndCreate(name, created);

          if (player == null) {
            plugin.getLogger().severe(name + " ban record creation failed, unable to lookup UUID");
            continue;
          }
        }

        PlayerMuteData ban = new PlayerMuteData(player, banActor, reason, false, expires, pastCreated);
        PlayerMuteRecord record = new PlayerMuteRecord(ban, actor, created);

        // Disallow duplicates
        if (plugin.getPlayerMuteRecordStorage().queryForMatchingArgs(record).size() != 0) continue;

        plugin.getPlayerMuteRecordStorage().create(record);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }
  }
}
