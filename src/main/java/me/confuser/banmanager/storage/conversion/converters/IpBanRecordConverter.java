package me.confuser.banmanager.storage.conversion.converters;

import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.IpBanRecord;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.IPUtils;

import java.sql.SQLException;

public class IpBanRecordConverter extends Converter {

  @Override
  public void run(DatabaseConnection connection) {
    DatabaseResults result;
    try {
      result = connection
              .compileStatement("SELECT banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time FROM " + conversionDb
                      .getTableName("ipBansRecordTable"), StatementBuilder.StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS)
              .runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    try {
      while (result.next()) {
        String ip = result.getString(0);
        String actorName = result.getString(1);
        String reason = result.getString(2);
        long pastCreated = result.getLong(3);
        long expires = result.getLong(4);
        String unbannedActorName = result.getString(5);
        long created = result.getLong(6);

        PlayerData banActor = playerStorage.retrieve(actorName, false);
        PlayerData actor = playerStorage.retrieve(unbannedActorName, false);

        if (actor == null) {
          actor = playerStorage.getConsole();
        }

        if (banActor == null) {
          banActor = playerStorage.getConsole();
        }

        IpBanData ban = new IpBanData(IPUtils.toLong(ip), banActor, reason, expires, pastCreated);
        IpBanRecord record = new IpBanRecord(ban, actor, created);

        plugin.getIpBanRecordStorage().create(record);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }
  }
}
