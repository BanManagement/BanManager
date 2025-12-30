package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.util.StorageUtils;

import java.sql.SQLException;

/**
 * Base storage class for BanManager that provides database-authoritative timestamps.
 * All storage classes that need cross-server sync should extend this instead of BaseDaoImpl.
 *
 * @param <T> the entity type
 * @param <ID> the ID type
 */
public abstract class BaseStorage<T, ID> extends BaseDaoImpl<T, ID> {

  protected final BanManagerPlugin plugin;
  private final DatabaseConfig dbConfig;

  protected BaseStorage(BanManagerPlugin plugin, ConnectionSource connectionSource,
                        DatabaseTableConfig<T> tableConfig, DatabaseConfig dbConfig) throws SQLException {
    super(connectionSource, tableConfig);
    this.plugin = plugin;
    this.dbConfig = dbConfig;
  }

  /**
   * @return the table name for this storage (for timestamp updates)
   */
  protected String getBmTableName() {
    return tableConfig.getTableName();
  }

  /**
   * @return true if this entity has an 'updated' column, false if only 'created'
   * Defaults to true; override and return false for entities with only 'created'
   */
  protected boolean hasUpdatedColumn() {
    return true;
  }

  /**
   * @return the DatabaseConfig for this storage (localDb or globalDb)
   */
  protected DatabaseConfig getDatabaseConfig() {
    return dbConfig;
  }

  /**
   * Creates an entity with database-authoritative timestamps.
   * After inserting, updates created/updated to UNIX_TIMESTAMP() and refreshes the entity.
   * This ensures cross-server sync works correctly regardless of JVM clock drift.
   */
  @Override
  public int create(T data) throws SQLException {
    int result = super.create(data);
    // extractId() is provided by BaseDaoImpl and uses OrmLite's field introspection
    int entityId = ((Number) extractId(data)).intValue();
    StorageUtils.updateTimestampsToDbTime(this, getDatabaseConfig(),
        getBmTableName(), entityId, hasUpdatedColumn());
    refresh(data);
    return result;
  }

  /**
   * Creates an entity preserving its Java-side timestamps.
   * Use this for imports where you want to keep the original timestamp values.
   */
  public int createPreservingTimestamps(T data) throws SQLException {
    return super.create(data);
  }

  /**
   * Updates an entity and sets the 'updated' column to database time.
   * This ensures cross-server sync works correctly regardless of JVM clock drift.
   */
  @Override
  public int update(T data) throws SQLException {
    int result = super.update(data);

    if (hasUpdatedColumn()) {
      int entityId = ((Number) extractId(data)).intValue();
      String nowExpr = getDatabaseConfig().getTimestampNow();
      String sql = "UPDATE `" + getBmTableName() + "` SET `updated` = " + nowExpr + " WHERE `id` = ?";
      executeRaw(sql, String.valueOf(entityId));
      refresh(data);
    }

    return result;
  }

  /**
   * Updates an entity preserving its Java-side timestamps.
   * Use this when you want to keep the existing timestamp values.
   */
  public int updatePreservingTimestamps(T data) throws SQLException {
    return super.update(data);
  }
}
