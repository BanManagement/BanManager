package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.runnables.BmRunnable;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SyncCommand extends BukkitCommand<BanManager> {
  private final String[] localSync = new String[]{ "playerBans", "playerMutes", "ipBans", "ipRangeBans", "expiresCheck" };
  private final String[] globalSync = new String[]{ "globalPlayerBans", "globalPlayerMutes", "globalPlayerNotes",
          "globalIpBans" };

  public SyncCommand() {
    super("bmsync");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length != 1) return false;

    final String type = args[0];

    if (!type.equals("local") && !type.equals("global")) return false;

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        Message.get("sync.player.started").set("type", type).sendTo(sender);

        if (type.equals("local")) {
          handleLocalSync();
        } else {
          handleGlobalSync();
        }

        Message.get("sync.player.finished").set("type", type).sendTo(sender);
      }
    });

    return true;
  }

  private void handleLocalSync() {
    for (int i = 0; i < localSync.length; i++) {
      BmRunnable runner = plugin.getSyncRunner().getRunner(localSync[i]);

      if (runner.isRunning()) continue;

      runner.run();
    }
  }

  private void handleGlobalSync() {
    if (plugin.getGlobalPlayerBanStorage() == null) return;

    for (int i = 0; i < globalSync.length; i++) {
      BmRunnable runner = plugin.getSyncRunner().getRunner(globalSync[i]);

      if (runner.isRunning()) continue;

      runner.run();
    }
  }
}
