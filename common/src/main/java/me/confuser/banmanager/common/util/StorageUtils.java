package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.field.SqlType;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.support.CompiledStatement;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;

import java.sql.SQLException;

public class StorageUtils {

  /**
   * Wraps an arbitrary exception as a {@link SQLException} for callers that
   * declare {@code throws SQLException}. Returns the original instance when
   * already a SQLException so the original message and stack trace are
   * preserved; otherwise wraps with the supplied context message.
   *
   * <p>Callers should use the {@code throw} keyword:</p>
   * <pre>
   *   try (CompiledStatement stmt = ...) { ... }
   *   catch (Exception e) { throw StorageUtils.toSqlException("Context", e); }
   * </pre>
   *
   * <p>This pattern exists because ORMLite 6.x's {@code AutoCloseable}
   * declarations throw the broader {@code Exception}, forcing every
   * try-with-resources around a statement to also catch {@code Exception}.</p>
   */
  public static SQLException toSqlException(String message, Exception e) {
    if (e instanceof SQLException sqle) return sqle;
    return new SQLException(message, e);
  }

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
    } catch (Exception e) {
      throw toSqlException("Failed to update timestamps", e);
    }
  }

}
