package me.confuser.banmanager.storage.global;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.global.GlobalPlayerNoteData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class GlobalPlayerNoteStorage extends BaseDaoImpl<GlobalPlayerNoteData, Integer> {

  public GlobalPlayerNoteStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<GlobalPlayerNoteData>) BanManager.getPlugin()
                                                                            .getGlobalDb().getTable("playerNotes"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
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
