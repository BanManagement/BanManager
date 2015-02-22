package me.confuser.banmanager.storage.conversion.converters;

import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.IPUtils;

import java.sql.SQLException;

public class IpBanConverter extends Converter {

  @Override
  public void run(DatabaseConnection connection) {
    DatabaseResults result;
    try {
      result = connection
              .compileStatement("SELECT banned, banned_by, ban_reason, ban_time, ban_expires_on FROM " + conversionDb
                      .getTableName("ipBansTable"), StatementBuilder.StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS)
              .runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    try {
      while (result.next()) {
        String ipStr = result.getString(0);
        long ip = IPUtils.toLong(ipStr);

        if (plugin.getIpBanStorage().isBanned(ip)) continue;

        String actorName = result.getString(1);
        String reason = result.getString(2);
        long created = result.getLong(3);
        long expires = result.getLong(4);

        PlayerData actor = playerStorage.retrieve(actorName, false);

        if (actor == null) {
          actor = playerStorage.getConsole();
        }

        IpBanData ban = new IpBanData(ip, actor, reason, expires, created);

        plugin.getIpBanStorage().create(ban);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }
  }
}
