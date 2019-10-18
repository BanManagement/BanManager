package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.runnables.BmRunnable;
import me.confuser.banmanager.common.util.Message;

public class SyncCommand extends CommonCommand {

  private final String[] localSync = new String[] { "playerBans", "playerMutes", "ipBans", "ipRangeBans", "expiresCheck" };
  private final String[] globalSync = new String[] { "externalPlayerBans", "externalPlayerMutes", "externalPlayerNotes",
          "externalIpBans" };

  public SyncCommand(BanManagerPlugin plugin) {
    super(plugin, "bmsync");
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length != 1) return false;

    final String type = parser.args[0];

    if (!type.equals("local") && !type.equals("global")) return false;

    getPlugin().getScheduler().runAsync(() -> {
      Message.get("sync.player.started").set("type", type).sendTo(sender);

      if (type.equals("local")) {
        handleLocalSync();
      } else {
        handleGlobalSync();
      }

      Message.get("sync.player.finished").set("type", type).sendTo(sender);
    });

    return true;
  }

  private void handleLocalSync() {
    for (String aLocalSync : localSync) {
      BmRunnable runner = getPlugin().getSyncRunner().getRunner(aLocalSync);

      if (runner.isRunning()) continue;

      runner.run();
    }
  }

  private void handleGlobalSync() {
    if (getPlugin().getGlobalPlayerBanStorage() == null) return;

    for (String aGlobalSync : globalSync) {
      BmRunnable runner = getPlugin().getSyncRunner().getRunner(aGlobalSync);

      if (runner.isRunning()) continue;

      runner.run();
    }
  }
}
