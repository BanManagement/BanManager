package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.CleanUp;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.IpMuteRecord;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.stmt.Where;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.util.StorageUtils;

import java.sql.SQLException;

public class IpMuteRecordStorage extends BaseStorage<IpMuteRecord, Integer> {

  @Override
  protected boolean hasUpdatedColumn() {
    return false;
  }

  public IpMuteRecordStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin, plugin.getLocalConn(), (DatabaseTableConfig<IpMuteRecord>) plugin.getConfig().getLocalDb()
        .getTable("ipMuteRecords"), plugin.getConfig().getLocalDb());

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      StorageUtils.convertIpColumn(plugin, tableConfig.getTableName(), "ip");

      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " ADD COLUMN `silent` TINYINT(1)");
      } catch (SQLException e) {
      }

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

  public IpMuteRecordStorage(BanManagerPlugin plugin, ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(plugin, connection, (DatabaseTableConfig<IpMuteRecord>) table, plugin.getConfig().getLocalDb());
  }

  public void addRecord(IpMuteData mute, PlayerData actor, String reason) throws SQLException {
    create(new IpMuteRecord(mute, actor, reason));
  }

  public CloseableIterator<IpMuteRecord> findUnmutes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    QueryBuilder<IpMuteRecord, Integer> query = queryBuilder();
    Where<IpMuteRecord, Integer> where = query.where();

    where.ge("created", fromTime);

    query.setWhere(where);

    return query.iterator();

  }

  public long getCount(IPAddress ip) throws SQLException {
    return queryBuilder().where().eq("ip", ip).countOf();
  }

  public CloseableIterator<IpMuteRecord> getRecords(IPAddress ip) throws SQLException {
    return queryBuilder().where().eq("ip", ip).iterator();
  }

  public void purge(CleanUp cleanup) throws SQLException {
    if (cleanup.getDays() == 0) return;

    updateRaw("DELETE FROM " + getTableInfo().getTableName() + " WHERE created < UNIX_TIMESTAMP(DATE_SUB(NOW(), " +
        "INTERVAL " + cleanup.getDays() + " DAY))");
  }
}
