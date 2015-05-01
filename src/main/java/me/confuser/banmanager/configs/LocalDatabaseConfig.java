package me.confuser.banmanager.configs;

import com.j256.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.data.*;
import org.bukkit.configuration.ConfigurationSection;

public class LocalDatabaseConfig extends DatabaseConfig {

  public LocalDatabaseConfig(ConfigurationSection conf) {
    super(conf);

    DatabaseTableConfig<PlayerData> playerTable = new DatabaseTableConfig<>(PlayerData.class, conf
            .getString("tables.players"), null);
    addTable("players", playerTable);

    DatabaseTableConfig<PlayerBanData> playerBansTable = new DatabaseTableConfig<>(PlayerBanData.class, conf
            .getString("tables.playerBans"), null);
    addTable("playerBans", playerBansTable);

    DatabaseTableConfig<PlayerBanRecord> playerBanRecordsTable = new DatabaseTableConfig<>(PlayerBanRecord.class, conf
            .getString("tables.playerBanRecords"), null);
    addTable("playerBanRecords", playerBanRecordsTable);

    DatabaseTableConfig<PlayerMuteData> playerMutesTable = new DatabaseTableConfig<>(PlayerMuteData.class, conf
            .getString("tables.playerMutes"), null);
    addTable("playerMutes", playerMutesTable);

    DatabaseTableConfig<PlayerMuteRecord> playerMuteRecordsTable = new DatabaseTableConfig<>(PlayerMuteRecord.class, conf
            .getString("tables.playerMuteRecords"), null);
    addTable("playerMuteRecords", playerMuteRecordsTable);

    DatabaseTableConfig<PlayerKickData> playerKicksTable = new DatabaseTableConfig<>(PlayerKickData.class, conf
            .getString("tables.playerKicks"), null);
    addTable("playerKicks", playerKicksTable);

    DatabaseTableConfig<PlayerWarnData> playerWarningsTable = new DatabaseTableConfig<>(PlayerWarnData.class, conf
            .getString("tables.playerWarnings"), null);
    addTable("playerWarnings", playerWarningsTable);

    DatabaseTableConfig<IpBanData> ipBansTable = new DatabaseTableConfig<>(IpBanData.class, conf
            .getString("tables.ipBans"), null);
    addTable("ipBans", ipBansTable);

    DatabaseTableConfig<IpBanRecord> ipBanRecordsTable = new DatabaseTableConfig<>(IpBanRecord.class, conf
            .getString("tables.ipBanRecords"), null);
    addTable("ipBanRecords", ipBanRecordsTable);

    DatabaseTableConfig<PlayerNoteData> playerNotesTable = new DatabaseTableConfig<>(PlayerNoteData.class, conf
            .getString("tables.playerNotes"), null);
    addTable("playerNotes", playerNotesTable);

    DatabaseTableConfig<IpRangeBanData> ipRangeBansTable = new DatabaseTableConfig<>(IpRangeBanData.class, conf
            .getString("tables.ipRangeBans"), null);
    addTable("ipRangeBans", ipRangeBansTable);

    DatabaseTableConfig<IpRangeBanRecord> ipRangeBanRecordsTable = new DatabaseTableConfig<>(IpRangeBanRecord.class, conf
            .getString("tables.ipRangeBanRecords"), null);
    addTable("ipRangeBanRecords", ipRangeBanRecordsTable);
  }

}
