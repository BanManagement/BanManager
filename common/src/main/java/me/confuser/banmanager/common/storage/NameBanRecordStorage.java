package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.NameBanRecord;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.stmt.Where;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.util.DateUtils;

import java.sql.SQLException;

public class NameBanRecordStorage extends BaseDaoImpl<NameBanRecord, Integer> {

  public NameBanRecordStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<NameBanRecord>) plugin.getConfig()
        .getLocalDb().getTable("nameBanRecords"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
      return;
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName()
          + " CHANGE `created` `created` BIGINT UNSIGNED,"
          + " CHANGE `pastCreated` `updated` BIGINT UNSIGNED,"
          + " CHANGE `expired` `expires` BIGINT UNSIGNED"
        );
      } catch (SQLException e) {
      }
    }
  }

  public NameBanRecordStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<NameBanRecord>) table);
  }

  public void addRecord(NameBanData ban, PlayerData actor, String reason) throws SQLException {
    create(new NameBanRecord(ban, actor, reason));
  }

  public CloseableIterator<NameBanRecord> findUnbans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<NameBanRecord, Integer> query = queryBuilder();
    Where<NameBanRecord, Integer> where = query.where();

    where.ge("created", checkTime);

    query.setWhere(where);


    return query.iterator();

  }
}
