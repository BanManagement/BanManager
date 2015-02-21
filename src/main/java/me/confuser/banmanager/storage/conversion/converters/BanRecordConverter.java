package me.confuser.banmanager.storage.conversion.converters;

import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerBanRecord;
import me.confuser.banmanager.data.PlayerData;

import java.sql.SQLException;

public class BanRecordConverter extends Converter {

  @Override
  public void run(DatabaseConnection connection) {
    DatabaseResults result;
    try {
      result = connection
              .compileStatement("SELECT banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time FROM " + conversionDb
                      .getTableName("bansRecordTable"), StatementBuilder.StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS)
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

        PlayerBanData ban = new PlayerBanData(player, banActor, reason, pastCreated, expires);
        PlayerBanRecord record = new PlayerBanRecord(ban, actor);

        // Disallow duplicates
        if (plugin.getPlayerBanRecordStorage().queryForMatchingArgs(record).size() != 0) continue;

        plugin.getPlayerBanRecordStorage().create(record);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }
  }
}
