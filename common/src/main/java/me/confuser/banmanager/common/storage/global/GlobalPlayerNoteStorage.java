package me.confuser.banmanager.common.storage.global;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.global.GlobalPlayerNoteData;
import me.confuser.banmanager.common.util.DateUtils;

import java.sql.SQLException;

public class GlobalPlayerNoteStorage extends BaseDaoImpl<GlobalPlayerNoteData, Integer> {

  public GlobalPlayerNoteStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getGlobalConn(), (DatabaseTableConfig<GlobalPlayerNoteData>) plugin.getConfig()
                                                                                   .getGlobalDb()
                                                                                   .getTable("playerNotes"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    }
  }

  public CloseableIterator<GlobalPlayerNoteData> findNotes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<GlobalPlayerNoteData, Integer> query = queryBuilder();
    query.setWhere(queryBuilder().where().ge("created", checkTime));

    return query.iterator();

  }
}
