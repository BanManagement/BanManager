package me.confuser.banmanager.common.runnables;


import me.confuser.banmanager.common.BanManagerPlugin;

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
