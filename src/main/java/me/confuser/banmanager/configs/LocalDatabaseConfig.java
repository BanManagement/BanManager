package me.confuser.banmanager.configs;

import me.confuser.banmanager.data.*;

import org.bukkit.configuration.ConfigurationSection;

import com.j256.ormlite.table.DatabaseTableConfig;

public class LocalDatabaseConfig extends DatabaseConfig {

	public LocalDatabaseConfig(ConfigurationSection conf) {
		super(conf);

		DatabaseTableConfig<PlayerData> playerTable = new DatabaseTableConfig<PlayerData>(PlayerData.class, conf.getString("tables.players"), null);
		addTable("players", playerTable);

		DatabaseTableConfig<PlayerBanData> playerBansTable = new DatabaseTableConfig<PlayerBanData>(PlayerBanData.class, conf.getString("tables.playerBans"), null);
		addTable("playerBans", playerBansTable);

		DatabaseTableConfig<PlayerBanRecord> playerBanRecordsTable = new DatabaseTableConfig<PlayerBanRecord>(PlayerBanRecord.class, conf.getString("tables.playerBanRecords"), null);
		addTable("playerBanRecords", playerBanRecordsTable);

		DatabaseTableConfig<PlayerMuteData> playerMutesTable = new DatabaseTableConfig<PlayerMuteData>(PlayerMuteData.class, conf.getString("tables.playerMutes"), null);
		addTable("playerMutes", playerMutesTable);
		
		DatabaseTableConfig<PlayerMuteRecord> playerMuteRecordsTable = new DatabaseTableConfig<PlayerMuteRecord>(PlayerMuteRecord.class, conf.getString("tables.playerMuteRecords"), null);
		addTable("playerMuteRecords", playerMuteRecordsTable);

		DatabaseTableConfig<PlayerKickData> playerKicksTable = new DatabaseTableConfig<PlayerKickData>(PlayerKickData.class, conf.getString("tables.playerKicks"), null);
		addTable("playerKicks", playerKicksTable);

		DatabaseTableConfig<PlayerWarnData> playerWarningsTable = new DatabaseTableConfig<PlayerWarnData>(PlayerWarnData.class, conf.getString("tables.playerWarnings"), null);
		addTable("playerWarnings", playerWarningsTable);

		DatabaseTableConfig<IpBanData> ipBansTable = new DatabaseTableConfig<IpBanData>(IpBanData.class, conf.getString("tables.ipBans"), null);
		addTable("ipBans", ipBansTable);

		DatabaseTableConfig<IpBanRecord> ipBanRecordsTable = new DatabaseTableConfig<IpBanRecord>(IpBanRecord.class, conf.getString("tables.ipBanRecords"), null);
		addTable("ipBanRecords", ipBanRecordsTable);
	}

}
