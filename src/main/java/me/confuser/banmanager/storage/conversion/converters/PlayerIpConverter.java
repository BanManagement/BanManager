package me.confuser.banmanager.storage.conversion.converters;

import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;

public class PlayerIpConverter extends Converter {

  @Override
  public void run(DatabaseConnection connection) {
    DatabaseResults result;
    try {
      result = connection.compileStatement("SELECT player, ip, last_seen FROM " + conversionDb
              .getTableName("playerIpsTable"), StatementBuilder.StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS)
                         .runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    try {
      while (result.next()) {
        String name = result.getString(0);
        long ip = result.getLong(1);
        long lastSeen = result.getLong(2);

        findAndCreate(name, lastSeen, ip);

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      result.closeQuietly();
    }
  }
}
