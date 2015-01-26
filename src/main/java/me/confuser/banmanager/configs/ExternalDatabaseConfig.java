package me.confuser.banmanager.configs;

import com.j256.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.data.external.*;
import org.bukkit.configuration.ConfigurationSection;

public class ExternalDatabaseConfig extends DatabaseConfig {

  public ExternalDatabaseConfig(ConfigurationSection conf) {
    super(conf);

    DatabaseTableConfig<ExternalPlayerBanData> playerBansTable = new DatabaseTableConfig<>(ExternalPlayerBanData.class, conf
            .getString("tables.playerBans"), null);
    addTable("playerBans", playerBansTable);

    DatabaseTableConfig<?> playerUnbansTable = new DatabaseTableConfig<>(ExternalPlayerBanRecordData
            .class,
            conf.getString("tables.playerUnbans"), null);
    addTable("playerUnbans", playerUnbansTable);

    DatabaseTableConfig<ExternalPlayerMuteData> playerMutesTable = new DatabaseTableConfig<>(ExternalPlayerMuteData
            .class, conf
            .getString("tables.playerMutes"), null);
    addTable("playerMutes", playerMutesTable);

    DatabaseTableConfig<ExternalPlayerMuteRecordData> playerUnmutesTable = new DatabaseTableConfig<>
            (ExternalPlayerMuteRecordData.class, conf.getString("tables.playerUnmutes"), null);
    addTable("playerUnmutes", playerUnmutesTable);

    DatabaseTableConfig<ExternalIpBanData> ipBansTable = new DatabaseTableConfig<>(ExternalIpBanData.class, conf
            .getString("tables.ipBans"), null);
    addTable("ipBans", ipBansTable);

    DatabaseTableConfig<ExternalIpBanRecordData> ipUnbansTable = new DatabaseTableConfig<>(ExternalIpBanRecordData.class, conf
            .getString("tables.ipUnbans"), null);
    addTable("ipUnbans", ipUnbansTable);
  }

}
