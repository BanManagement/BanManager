package me.confuser.banmanager.configs;

import me.confuser.banmanager.data.*;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

public class LocalDatabaseConfig extends DatabaseConfig {

  private static HashMap<String, Class> types = new HashMap<String, Class>() {{
    put("players", PlayerData.class);

    put("playerBans", PlayerBanData.class);
    put("playerBanRecords", PlayerBanRecord.class);

    put("playerMutes", PlayerMuteData.class);
    put("playerMuteRecords", PlayerMuteRecord.class);

    put("playerKicks", PlayerKickData.class);

    put("playerNotes", PlayerNoteData.class);

    put("playerHistory", PlayerHistoryData.class);

    put("playerReports", PlayerReportData.class);
    put("playerReportLocations", PlayerReportLocationData.class);
    put("playerReportCommands", PlayerReportCommandData.class);
    put("playerReportComments", PlayerReportCommentData.class);
    put("playerReportStates", ReportState.class);

    put("playerWarnings", PlayerWarnData.class);

    put("ipBans", IpBanData.class);
    put("ipBanRecords", IpBanRecord.class);

    put("ipMutes", IpMuteData.class);
    put("ipMuteRecords", IpMuteRecord.class);

    put("ipRangeBans", IpRangeBanData.class);
    put("ipRangeBanRecords", IpRangeBanRecord.class);

    put("rollbacks", RollbackData.class);
  }};

  public LocalDatabaseConfig(ConfigurationSection conf) {
    super(conf, types);
  }

}
