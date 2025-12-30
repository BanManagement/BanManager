package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.NameBanRecord;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.stmt.Where;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;

import java.sql.SQLException;

public class NameBanRecordStorage extends BaseStorage<NameBanRecord, Integer> {

  @Override
  protected boolean hasUpdatedColumn() {
    return false;
  }

  public NameBanRecordStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin, plugin.getLocalConn(), (DatabaseTableConfig<NameBanRecord>) plugin.getConfig()
        .getLocalDb().getTable("nameBanRecords"), plugin.getConfig().getLocalDb());

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
      return;
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName()
          + " CHANGE `created` `created` BIGINT UNSIGNED,"
          + " CHANGE `pastCreated` `pastCreated` BIGINT UNSIGNED,"
          + " CHANGE `expired` `expired` BIGINT UNSIGNED"
        );
      } catch (SQLException e) {
      }
    }
  }

  public NameBanRecordStorage(BanManagerPlugin plugin, ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(plugin, connection, (DatabaseTableConfig<NameBanRecord>) table, plugin.getConfig().getLocalDb());
  }

  public void addRecord(NameBanData ban, PlayerData actor, String reason) throws SQLException {
    create(new NameBanRecord(ban, actor, reason));
  }

  public CloseableIterator<NameBanRecord> findUnbans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    QueryBuilder<NameBanRecord, Integer> query = queryBuilder();
    Where<NameBanRecord, Integer> where = query.where();

    where.ge("created", fromTime);

    query.setWhere(where);


    return query.iterator();

  }
}
