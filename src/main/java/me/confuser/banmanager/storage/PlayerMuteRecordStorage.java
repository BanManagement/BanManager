package me.confuser.banmanager.storage;

import java.sql.SQLException;

import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.data.PlayerMuteRecord;
import me.confuser.banmanager.util.DateUtils;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

public class PlayerMuteRecordStorage extends BaseDaoImpl<PlayerMuteRecord, Integer> {

	public PlayerMuteRecordStorage(ConnectionSource connection, DatabaseTableConfig<PlayerMuteRecord> tableConfig) throws SQLException {
		super(connection, tableConfig);
	}

	public void addRecord(PlayerMuteData mute, PlayerData actor) throws SQLException {
		create(new PlayerMuteRecord(mute, actor));
	}

	public CloseableIterator<PlayerMuteRecord> findUnmutes(long fromTime) throws SQLException {
		if (fromTime == 0) {
			return iterator();
		}

		long checkTime = fromTime + DateUtils.getTimeDiff();

		QueryBuilder<PlayerMuteRecord, Integer> query = queryBuilder();
		Where<PlayerMuteRecord, Integer> where = query.where();

		where.ge("created", checkTime);

		query.setWhere(where);

		return query.iterator();

	}

	public long getCount(PlayerData player) throws SQLException {
		return queryBuilder().where().eq("player_id", player).countOf();
	}

	public CloseableIterator<PlayerMuteRecord> getRecords(PlayerData player) throws SQLException {
		return queryBuilder().where().eq("player_id", player).iterator();
	}

}
