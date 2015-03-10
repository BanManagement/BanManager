package me.confuser.banmanager.util;

import me.confuser.banmanager.BanManager;
import net.gravitydevelopment.updater.Updater;

import java.io.File;

public class UpdateUtils {

  public static boolean isUpdateAvailable(File jarFile) {
    Updater updater = new Updater(BanManager.getPlugin(), 41473, jarFile, Updater.UpdateType.NO_DOWNLOAD, false);

    return updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
  }
}
