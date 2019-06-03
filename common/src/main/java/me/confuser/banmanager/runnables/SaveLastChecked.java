package me.confuser.banmanager.runnables;

import me.confuser.banmanager.common.plugin.BanManagerPlugin;

public class SaveLastChecked implements Runnable {

      private BanManagerPlugin plugin;

      public SaveLastChecked(BanManagerPlugin plugin) {
            this.plugin = plugin;
      }

      @Override
      public void run() {
            plugin.getSchedulesConfig().save();
      }

}
