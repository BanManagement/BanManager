package me.confuser.banmanager.common.util;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.common.BanManagerPlugin;

import java.io.IOException;
import java.sql.SQLException;

public class StorageUtils {

  public static void convertIpColumn(BanManagerPlugin plugin, String table, String column) {
    convertIpColumn(plugin, table, column, "int");
  }

  public static void convertIpColumn(BanManagerPlugin plugin, String table, String column, String idType) {
    try (DatabaseConnection connection = plugin.getLocalConn().getReadWriteConnection(table)) {
      if (connection.update("ALTER TABLE `" + table + "` CHANGE COLUMN `" + column + "` `" + column + "` VARBINARY(16) NOT NULL", null, null) != 0) {
          plugin.getLogger().info("Converting " + table + " " + column + " data to support ipv6");

        DatabaseResults results = connection
            .compileStatement("SELECT `id`, INET_NTOA(HEX(UNHEX(CAST(" + column + " AS UNSIGNED)))) FROM `" + table + "`", StatementBuilder
                .StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS, false)
            .runQuery(null);

        while (results.next()) {
          CompiledStatement statement = connection
              .compileStatement("UPDATE " + table + " SET `" + column + "` = ? WHERE `id` = ?", StatementBuilder
                  .StatementType.UPDATE, null, DatabaseConnection.DEFAULT_RESULT_FLAGS, false);

          Object id;

          if (idType.equals("int")) {
            id = results.getInt(0);
          } else {
            id = results.getBytes(0);
          }

          String ipStr = results.getString(1);
          byte[] ip = IPUtils.toBytes(ipStr);

          statement.setObject(0, ip, SqlType.BYTE_ARRAY);
          statement.setObject(1, id, idType.equals("int") ? SqlType.INTEGER : SqlType.BYTE_ARRAY);

          if (statement.runUpdate() == 0) {
            plugin.getLogger().severe("Unable to convert " + ipStr + " in " + table + " for id " + id);
          }
        }
      }
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    }
  }
}
