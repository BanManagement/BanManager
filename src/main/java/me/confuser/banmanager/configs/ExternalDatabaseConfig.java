package me.confuser.banmanager.configs;

import me.confuser.banmanager.data.external.*;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

public class ExternalDatabaseConfig extends DatabaseConfig {

  private static HashMap<String, Class> types = new HashMap<String, Class>() {{
    put("playerBans", ExternalPlayerBanData.class);
    put("playerUnbans", ExternalPlayerBanRecordData.class);

    put("playerMutes", ExternalPlayerMuteData.class);
    put("playerUnmutes", ExternalPlayerMuteRecordData.class);

    put("playerNotes", ExternalPlayerNoteData.class);

    put("ipBans", ExternalIpBanData.class);
    put("ipUnbans", ExternalIpBanRecordData.class);
  }};

  public ExternalDatabaseConfig(ConfigurationSection conf) {
    super(conf, types);
  }

}
