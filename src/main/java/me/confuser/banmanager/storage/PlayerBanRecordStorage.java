package me.confuser.banmanager.storage;

import java.sql.SQLException;

import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerBanRecord;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.DateUtils;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

public class PlayerBanRecordStorage extends BaseDaoImpl<PlayerBanRecord, Integer> {

	public PlayerBanRecordStorage(ConnectionSource connection, DatabaseTableConfig<PlayerBanRecord> tableConfig) throws SQLException {
		super(connection, tableConfig);
	}

	public void addRecord(PlayerBanData ban, PlayerData actor) throws SQLException {
		create(new PlayerBanRecord(ban, actor));
	}
	
	public CloseableIterator<PlayerBanRecord> findUnbans(long fromTime) throws SQLException {
		if (fromTime == 0)
			return iterator();
		
		long checkTime = fromTime + DateUtils.getTimeDiff();
		
		QueryBuilder<PlayerBanRecord, Integer> query = queryBuilder();
		Where<PlayerBanRecord, Integer> where = query.where();
		
		where.ge("created", checkTime);
		
		query.setWhere(where);
		
		return query.iterator();
		
	}
	
	public long getCount(PlayerData player) throws SQLException {
		return queryBuilder().where().eq("player_id", player).countOf();
	}
}
