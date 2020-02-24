package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.data.global.*;
import me.confuser.banmanager.common.configuration.ConfigurationSection;

import java.io.File;
import java.util.HashMap;

public class GlobalDatabaseConfig extends DatabaseConfig {

  private static HashMap<String, Class> types = new HashMap<String, Class>() {{
    put("playerBans", GlobalPlayerBanData.class);
    put("playerUnbans", GlobalPlayerBanRecordData.class);

    put("playerMutes", GlobalPlayerMuteData.class);
    put("playerUnmutes", GlobalPlayerMuteRecordData.class);

    put("playerNotes", GlobalPlayerNoteData.class);

    put("ipBans", GlobalIpBanData.class);
    put("ipUnbans", GlobalIpBanRecordData.class);
  }};

  public GlobalDatabaseConfig(File dataFolder, ConfigurationSection conf) {
    super(dataFolder, conf, types);
  }

}
