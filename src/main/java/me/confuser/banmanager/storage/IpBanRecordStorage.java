package me.confuser.banmanager.storage;

import java.sql.SQLException;

import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.IpBanRecord;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.DateUtils;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import java.util.ArrayList;
import java.util.List;

public class IpBanRecordStorage extends BaseDaoImpl<IpBanRecord, Integer> {

	public IpBanRecordStorage(ConnectionSource connection, DatabaseTableConfig<IpBanRecord> tableConfig) throws SQLException {
		super(connection, tableConfig);
	}

	public void addRecord(IpBanData ban, PlayerData actor) throws SQLException {
		create(new IpBanRecord(ban, actor));
	}
	
	public List<IpBanRecord> findUnbans(long fromTime) throws SQLException {
		if (fromTime == 0)
			return new ArrayList<>();
		
		long checkTime = fromTime + DateUtils.getTimeDiff();
		
		QueryBuilder<IpBanRecord, Integer> query = queryBuilder();
		Where<IpBanRecord, Integer> where = query.where();
		
		where.ge("created", checkTime);
		
		query.setWhere(where);
		
		return query.query();
	}
	
	public long getCount(long ip) throws SQLException {
		return queryBuilder().where().eq("ip", ip).countOf();
	}
}
