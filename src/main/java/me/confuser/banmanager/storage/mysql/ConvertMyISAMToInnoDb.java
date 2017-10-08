package me.confuser.banmanager.storage.mysql;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.BanManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ConvertMyISAMToInnoDb {
  private BanManager plugin = BanManager.getPlugin();

  public ConvertMyISAMToInnoDb(ConnectionSource db, HashMap<String, DatabaseTableConfig<?>> tables) {
    DatabaseConnection connection = null;

    try {
      connection = db.getReadWriteConnection();
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    for (Map.Entry<String, DatabaseTableConfig<?>> entry : tables.entrySet()) {
      final DatabaseResults result;
      final String table = entry.getValue().getTableName();

      try {
        CompiledStatement statement = connection.compileStatement("SHOW TABLE STATUS WHERE Name = ?", StatementBuilder
                .StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS);

        statement.setObject(0, table, SqlType.STRING);

        result = statement.runQuery(null);
      } catch (SQLException e) {
        e.printStackTrace();
        continue;
      }

      try {
        while (result.next()) {
          final String engine = result.getString(1);

          if (engine.equals("MyISAM")) {
            plugin.getLogger().info("Converting " + entry.getKey() + " table from MyISAM to InnoDB");

            connection.executeStatement("ALTER TABLE " + table + " ENGINE=InnoDB;", DatabaseConnection.DEFAULT_RESULT_FLAGS);
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        result.closeQuietly();
      }

    }

    connection.closeQuietly();
  }

}
