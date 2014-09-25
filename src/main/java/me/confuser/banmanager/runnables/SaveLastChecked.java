package me.confuser.banmanager.runnables;

import me.confuser.banmanager.BanManager;

public class SaveLastChecked implements Runnable {

      private BanManager plugin = BanManager.getPlugin();

      @Override
      public void run() {
            plugin.getSchedulesConfig().save();
      }

}
