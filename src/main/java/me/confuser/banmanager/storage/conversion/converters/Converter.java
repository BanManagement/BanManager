package me.confuser.banmanager.storage.conversion.converters;

import com.j256.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.ConvertDatabaseConfig;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.storage.PlayerStorage;
import me.confuser.banmanager.util.UUIDProfile;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;

public abstract class Converter {

  protected BanManager plugin = BanManager.getPlugin();
  protected PlayerStorage playerStorage = plugin.getPlayerStorage();
  protected ConvertDatabaseConfig conversionDb = plugin.getConfiguration().getConversionDb();

  public Converter() {
  }

  public abstract void run(DatabaseConnection connection);

  public PlayerData findAndCreate(String name, long lastSeen) {
    return findAndCreate(name, lastSeen, 2130706433);
  }

  public PlayerData findAndCreate(String name, long lastSeen, long ip) {
    UUIDProfile profile;
    try {
      profile = UUIDUtils.getUUIDProfile(name, lastSeen);
    } catch (Exception e) {
      return null;
    }

    if (profile == null) return null;

    PlayerData data = new PlayerData(profile.getUuid(), profile.getName(), ip, lastSeen);

    try {
      playerStorage.createOrUpdate(data);
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }

    if (BanManager.getPlugin().getConfiguration().isOnlineMode()) {
      try {
        Thread.sleep(1010L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return data;
  }
}
