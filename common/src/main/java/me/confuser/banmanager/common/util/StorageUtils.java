package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.field.SqlType;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.support.CompiledStatement;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;

import java.io.IOException;
import java.sql.SQLException;

public class StorageUtils {

  /**
   * Updates the created and updated timestamps to database time using the DAO.
   * This ensures timestamps are always from the database, not the JVM, for cross-server sync.
   * Uses the DAO's executeRaw to avoid connection pool issues.
   *
   * @param dao the DAO to use for executing the update
   * @param dbConfig the database config (provides timestamp expression)
   * @param tableName the table name
   * @param id the generated ID of the inserted row
   * @param hasUpdated whether the table has an 'updated' column
   * @throws SQLException if the update fails
   */
  public static void updateTimestampsToDbTime(BaseDaoImpl<?, ?> dao, DatabaseConfig dbConfig,
                                               String tableName, int id, boolean hasUpdated) throws SQLException {
    String nowExpr = dbConfig.getTimestampNow();
    String sql;
    if (hasUpdated) {
      sql = "UPDATE `" + tableName + "` SET `created` = " + nowExpr + ", `updated` = " + nowExpr + " WHERE `id` = " + id;
    } else {
      sql = "UPDATE `" + tableName + "` SET `created` = " + nowExpr + " WHERE `id` = " + id;
    }

    dao.executeRawNoArgs(sql);
  }

  // Legacy method for backwards compatibility
  public static void updateTimestampsToDbTime(ConnectionSource connectionSource, DatabaseConfig dbConfig,
                                               String tableName, int id, boolean hasUpdated) throws SQLException {
    String nowExpr = dbConfig.getTimestampNow();
    String sql;
    if (hasUpdated) {
      sql = "UPDATE `" + tableName + "` SET `created` = " + nowExpr + ", `updated` = " + nowExpr + " WHERE `id` = ?";
    } else {
      sql = "UPDATE `" + tableName + "` SET `created` = " + nowExpr + " WHERE `id` = ?";
    }

    try (DatabaseConnection connection = connectionSource.getReadWriteConnection(tableName)) {
      CompiledStatement statement = connection.compileStatement(sql, StatementBuilder.StatementType.UPDATE,
          null, DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
      statement.setObject(0, id, SqlType.INTEGER);
      statement.runUpdate();
    } catch (IOException e) {
      throw new SQLException("Failed to update timestamps", e);
    }
  }

  public static void convertIpColumn(BanManagerPlugin plugin, String table, String column) {
    convertIpColumn(plugin, table, column, "int");
  }

  public static void convertIpColumn(BanManagerPlugin plugin, String table, String column, String idType) {
    try (DatabaseConnection connection = plugin.getLocalConn().getReadWriteConnection(table)) {
      if (connection.update("ALTER TABLE `" + table + "` CHANGE COLUMN `" + column + "` `" + column + "` VARBINARY(16) NOT NULL", null, null) != 0) {
        plugin.getLogger().info("Converting " + table + " " + column + " data to support IPv6");

        plugin.getLogger().info("Attempting fast IPv6 conversion...");

        try {
          if (connection
            .compileStatement("UPDATE `" + table + "` SET " + column + " = INET6_ATON(INET_NTOA(" + column + "))", StatementBuilder
                .StatementType.UPDATE, null, DatabaseConnection.DEFAULT_RESULT_FLAGS, false)
            .runUpdate() == 0) {
              throw new SQLException("Failed to fast convert, attempting slow conversion...");
            } else {
              plugin.getLogger().info("Successfully converted " + table + " " + column + " data to support IPv6");
            }
        } catch (Exception e) {
          plugin.getLogger().severe("Failed to fast convert due to " + e.getMessage() + ", attempting slow conversion...");

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
      }
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    }
  }
}
