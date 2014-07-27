package me.confuser.banmanager.storage;

import java.sql.SQLException;

import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerBanRecord;
import me.confuser.banmanager.data.PlayerData;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

public class PlayerBanRecordStorage extends BaseDaoImpl<PlayerBanRecord, Integer> {

	public PlayerBanRecordStorage(ConnectionSource connection, DatabaseTableConfig<PlayerBanRecord> tableConfig) throws SQLException {
		super(connection, tableConfig);
	}

	public void addRecord(PlayerBanData ban, PlayerData actor) throws SQLException {
		create(new PlayerBanRecord(ban, actor));
	}
}
